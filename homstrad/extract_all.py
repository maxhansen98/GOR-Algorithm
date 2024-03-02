#!/usr/bin/python3
# Programmierpraktikum WS2023/2024
# Uebungsblatt 2, Aufgabe 14 (HOMSTRAD)
# Malte A. Weyrich

import os
from collections import defaultdict
import mysql.connector
from mysql.connector import errorcode

cnx = mysql.connector.connect(user='bioprakt3', password='$1$dXmWsf6J$rQWMUrRzyAhhqjPscdRbG.',
                          host='mysql2-ext.bio.ifi.lmu.de',
                          database='bioprakt3',
                          port='3306')
cursor = cnx.cursor()

#  TODO: maybe also collect meta data like species or sequence length
def get_db(path_to_homstrad):
    """
    reads in entire HOMSTRAD directory and creates two dicts
    :path_to_homstrad: path pointing to HOMSTRAD
    """

    items = os.listdir(path_to_homstrad)
    subfamily_dirs = [item for item in items if os.path.isdir(os.path.join(path_to_homstrad, item))]
    
    # id_to_sup_fams:= {pdb_id: (sup_fams*)}
    id_to_sup_fams = defaultdict(set) 
    
    # sup_fams:= {sup_fam: [(pbd_id, ali_seq, sec_seq)*]}
    sup_fams = defaultdict(set)

    # helper_dict
    ali_seq_dict = {}
    sec_seq_dict = {}
    organism_dict = {}
    

    # iterate over each super family (sup_fam)
    for curr_sup_fam in subfamily_dirs:
        path_to_dir = os.path.join(path_to_homstrad, curr_sup_fam)
        files = os.listdir(path_to_dir)
        
        # very lazy
        tem_file = [file for file in files if file[-1] == "m"]  # tem ends with m xD
        ali_file = [file for file in files if file[-1] == "i"]  # ali ends with i xD

        # get alignments
        tem_file = os.path.join(path_to_dir, tem_file[0])
        ali_file = os.path.join(path_to_dir, ali_file[0])
        
        content_tem = []

        # read .tem
        with open(tem_file) as tem:
            content_tem = tem.readlines()

        # read .ali
        with open(ali_file) as ali:
            content_ali = ali.readlines()

        # vars needed for iterations
        current_pbd_id = None
        formatted_sec_seq = None
        formatted_ali_seq = None

        # deals with .tem file
        for i in range(len(content_tem)):
            line = content_tem[i].rstrip()

            # prevent index error
            if line == "":
                break

            # extract pbd
            if line[0] == ">":
                current_pbd_id = line.split(";")[1] #[:-1]  
                # store subfam for curr id
                id_to_sup_fams[current_pbd_id].add(curr_sup_fam)

            # get sec struct
            elif line.startswith("secondary structure"):
                in_sec_struct = True
                sec_seq = ""
                j = 1
                while in_sec_struct:
                    if content_tem[i+j].startswith(">"):
                        in_sec_struct = False
                    else:
                        sec_seq+=content_tem[i+j].strip("\n")
                        j+=1
                # store sec_seq
                formatted_sec_seq = sec_seq.strip("\n")
                sec_seq_dict[(curr_sup_fam, current_pbd_id)] = formatted_sec_seq
            
            # get sequence
            elif line.startswith("sequence"):
                in_seq = True
                seq = ""
                j = 1
                while in_seq:
                    if content_tem[i+j].startswith(">"):
                        in_seq = False
                    else:
                        seq+=content_tem[i+j].strip("\n")
                        j+=1
                formatted_ali_seq = seq.strip("\n")
                ali_seq_dict[(curr_sup_fam, current_pbd_id)] = formatted_ali_seq


        for i in range(len(content_ali)):
            line = content_ali[i].rstrip()
            if line.startswith(">"): 
                current_pbd_id = line.split(";")[1]
                
                # for pbd_ids with chain info also create entry without chain
                pbd_id_no_chain = current_pbd_id
                if len(current_pbd_id) == 5:
                    # remove chain (5th char)
                    pbd_id_no_chain = current_pbd_id[:-1]

                if content_ali[i+1].startswith(f"structureX:{pbd_id_no_chain}:"):
                    organism_row  = content_ali[i+1].split(":")
                    organism = organism_row[-3]  # get organism
                    organism_dict[current_pbd_id] = organism
    
        # append all relevant vals to sup_fam_dict
        for sup_fam_pbd_id_tup in ali_seq_dict.keys():
            sup_fam_entry = (sup_fam_pbd_id_tup[1], ali_seq_dict[sup_fam_pbd_id_tup], sec_seq_dict[sup_fam_pbd_id_tup])
            sup_fams[sup_fam_pbd_id_tup[0]].add(sup_fam_entry)

    # for id, organism in organism_dict.items():    
    #     print(id, organism)

    return sup_fams, id_to_sup_fams, organism_dict
    
def insert_os(organism_dict: dict):
    """Inserts organisms into db os table
    """

    for pbd_id, organism in organism_dict.items():
        # insert organism
        add_organism = ("INSERT INTO Organisms "
                        "(name) "
                        "VALUES (%s)")
        data_organism = (organism,)
        cursor.execute(add_organism, data_organism)


def insert_into_db(sup_fams: dict, organism_dict: dict):
    # insert organisms
    # for sup_fam, entries in sup_fams.items():
    #     for entry in entries:
    #         pbd_id, ali_seq, sec_seq = entry
    #         
    #         # insert into homstrad
    #         add_homstrad = ("INSERT INTO Homestrad "
    #                         "(family, alignment) "
    #                         "VALUES (%s, %s)")
    #         data_homstrad = (sup_fam, ali_seq)
    #         cursor.execute(add_homstrad, data_homstrad)
    #         cnx.commit()


    pass

if __name__ == "__main__":
    sup_fams, id_to_sup_fams, organism_dict = get_db("./HOMSTRAD/")

    # insert organisms
    insert_os(organism_dict)



# DATABASE SCHEMA:

# MariaDB [bioprakt3]> DESCRIBE Homestrad;
# +-----------+--------------+------+-----+---------+----------------+
# | Field     | Type         | Null | Key | Default | Extra          |
# +-----------+--------------+------+-----+---------+----------------+
# | id        | int(11)      | NO   | PRI | NULL    | auto_increment |
# | family    | varchar(250) | YES  |     | NULL    |                |
# | alignment | longtext     | NO   |     | NULL    |                |
# +-----------+--------------+------+-----+---------+----------------+

# MariaDB [bioprakt3]> DESCRIBE Sequences;
# +-----------+--------------+------+-----+---------+----------------+
# | Field     | Type         | Null | Key | Default | Extra          |
# +-----------+--------------+------+-----+---------+----------------+
# | id        | int(11)      | NO   | PRI | NULL    | auto_increment |
# | type      | varchar(250) | YES  |     | NULL    |                |
# | sequence  | longtext     | NO   |     | NULL    |                |
# | source    | varchar(250) | YES  |     | NULL    |                |
# | source_id | varchar(250) | YES  |     | NULL    |                |
# | os_id     | int(11)      | YES  | MUL | NULL    |                |
# +-----------+--------------+------+-----+---------+----------------+

# MariaDB [bioprakt3]> DESCRIBE Alignments;
# +-----------+--------------+------+-----+---------+----------------+
# | Field     | Type         | Null | Key | Default | Extra          |
# +-----------+--------------+------+-----+---------+----------------+
# | id        | int(11)      | NO   | PRI | NULL    | auto_increment |
# | almnt_id  | int(11)      | NO   | MUL | NULL    |                |
# | prot_head | varchar(250) | NO   | MUL | NULL    |                |
# +-----------+--------------+------+-----+---------+----------------+

# MariaDB [bioprakt3]> show tables;
# +---------------------+
# | Tables_in_bioprakt3 |
# +---------------------+
# | Alignments          |
# | Homestrad           |
# | Keywords            |
# | Organisms           |
# | Sequences           |
# +---------------------+

# MariaDB [bioprakt3]> describe Organisms;
# +-------+--------------+------+-----+---------+----------------+
# | Field | Type         | Null | Key | Default | Extra          |
# +-------+--------------+------+-----+---------+----------------+
# | id    | int(11)      | NO   | PRI | NULL    | auto_increment |
# | name  | varchar(250) | YES  |     | NULL    |                |
# +-------+--------------+------+-----+---------+----------------+
