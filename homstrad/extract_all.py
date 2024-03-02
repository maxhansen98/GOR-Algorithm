#!/usr/bin/python3
# Programmierpraktikum WS2023/2024
# Uebungsblatt 2, Aufgabe 14 (HOMSTRAD)
# Malte A. Weyrich

# DATABASE SCHEMA:

# MariaDB [bioprakt3]> DESCRIBE Homstrad;
# +-----------+--------------+------+-----+---------+----------------+
# | Field     | Type         | Null | Key | Default | Extra          |
# +-----------+--------------+------+-----+---------+----------------+
# | id        | int(11)      | NO   | PRI | NULL    | auto_increment |
# | family    | varchar(250) | YES  |     | NULL    |                |
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
# | Homstrad            |
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
import os
from collections import defaultdict
import mysql.connector

cnx = mysql.connector.connect(user='bioprakt3', password='$1$dXmWsf6J$rQWMUrRzyAhhqjPscdRbG.',
                          host='mysql2-ext.bio.ifi.lmu.de',
                          database='bioprakt3',
                          port='3306')
cursor = cnx.cursor()

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
        current_pdb_id = None
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
                current_pdb_id = line.split(";")[1] #[:-1]  
                # store subfam for curr id
                id_to_sup_fams[current_pdb_id].add(curr_sup_fam)

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
                sec_seq_dict[(curr_sup_fam, current_pdb_id)] = formatted_sec_seq
            
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
                ali_seq_dict[(curr_sup_fam, current_pdb_id)] = formatted_ali_seq


        # deals with .ali file
        for i in range(len(content_ali)):
            line = content_ali[i].rstrip()
            if line.startswith(">"): 
                current_pdb_id = line.split(";")[1]
                # if current_pdb_id == "1adr":
                #     if content_ali[i+1].startswith(f"structure"):
                #         print(content_ali[i+1].split(":"))
                
                # for pbd_ids with chain info also create entry without chain
                pdb_id_no_chain = current_pdb_id
                if len(current_pdb_id) == 5:
                    # remove chain (5th char)
                    pdb_id_no_chain = current_pdb_id[:-1]

                if content_ali[i+1].startswith(f"structure"):
                    organism_row  = content_ali[i+1].split(":")
                    organism = organism_row[-3]  # get organism
                    organism_dict[pdb_id_no_chain] = organism
    
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
    organisms_to_insert = set(organism_dict.values())
    for organism in organisms_to_insert:
        # check if organism is already in db
        cursor.execute("SELECT * FROM Organisms WHERE name = %s", (organism,))
        if cursor.fetchone():
            print(f"{organism} already in db")
            continue
        # insert organism
        add_organism = ("INSERT INTO Organisms "
                        "(name) "
                        "VALUES (%s)")
        data_organism = (organism,)
        cursor.execute(add_organism, data_organism)


def get_os_id_dict(os_dict: dict):
    pdb_id_to_os_id = {}

    # iterate over pdb_id and organism in os_dict
    for pdb_id, organism in os_dict.items():
        # get os_id
        cursor.execute("SELECT id FROM Organisms WHERE name = %s", (organism,))
        os_id = cursor.fetchone()

        # Check if os_id is fetched correctly
        if os_id is not None:
            # Extracting the ID value from the result
            os_id = os_id[0]
            pdb_id_to_os_id[pdb_id] = os_id
        else:
            # Handle case where organism name is not found in the database
            # You can raise an exception or handle it according to your requirement
            print(f"Organism '{organism}' not found in the database for PDB ID '{pdb_id}'")

    return pdb_id_to_os_id
    """Returns a dict with os_id as key and organism as value
    :os_dict: '1ftra1': 'Methanopyrus kandleri', ....
    """

    pdb_id_to_os_id = {}

    # iterate ofer pdb_id and organism in os_dict
    for pdb_id, organism in os_dict.items():
        # get os_id
        cursor.execute("SELECT id FROM Organisms WHERE name = %s", (organism,))
        os_id = cursor.fetchone()

        pdb_id_to_os_id[pdb_id] = os_id

    return os_dict

    # check
    # for key, val in sup_fam_to_os_id.items():
    #     print(key, val)


# def insert_alignments(sup_fams: dict, pdb_id_to_os_id: dict):
#     """Inserts all alignments into db and checks if they are already in the db
#     :sup_fams: dict with super families and their entries
#     :pdb_id_to_os_id: dict with pdb_id to os_id
#     """
#     
#     ali_id_counter = 1
#     for sup_fam, entries in sup_fams.items():
#         for entry in entries:
#             pbd_id, ali_seq, sec_seq = entry
#
#             # check if family is alrady in homstrad
#             cursor.execute("SELECT * FROM Homstrad WHERE family = %s", (sup_fam,))
#             if cursor.fetchone():
#                 print(f"{sup_fam} already in homstrad")
#             
#             else:
#                 # insert into homstrad:
#                 # id (auto_increment), super_family, alignment_id
#                 add_homstrad = ("INSERT INTO Homstrad "
#                                 "(family) "
#                                 "VALUES (%s)")
#                 data_homstrad = (sup_fam)
#                 cursor.execute(add_homstrad, data_homstrad)
#             
#             # check if ali_seq is alrady in sequences
#             cursor.execute("SELECT * FROM Sequences WHERE sequence = %s", (ali_seq,))
#             if cursor.fetchone():
#                 print(f"{ali_seq} already in sequences")
#             else:
#                 # insert ali into sequences:
#                 # id (auto_increment), type=homstrad_alignment, sequence=ali_seq, source=HOMSTRAD, source_id=pdb_id, os_id
#                 add_ali_seq = ("INSERT INTO Sequences "
#                            "(type, sequence, source, source_id, os_id) "
#                            "VALUES (%s, %s, %s, %s, %s)")
#                 data_seq = ("seq_ali", ali_seq, "HOMSTRAD", pbd_id, pdb_id_to_os_id[pbd_id])
#                 cursor.execute(add_ali_seq, data_seq)
#             
#             # check if sec_seq is alrady in sequences
#             cursor.execute("SELECT * FROM Sequences WHERE sequence = %s", (sec_seq,))
#             if cursor.fetchone():
#                 print(f"{sec_seq} already in sequences")
#             else:
#                 # insert sec into sequences
#                 add_sec_seq = ("INSERT INTO Sequences "
#                            "(type, sequence, source, source_id, os_id) "
#                            "VALUES (%s, %s, %s, %s, %s)")
#                 data_seq = ("sec_ali", sec_seq, "HOMSTRAD", pbd_id, 1)
#                 cursor.execute(add_sec_seq, data_seq)
#
#             # insert pbd_id and ali_id into alignments
#             # id (auto_increment), ali_id, prot_head
#             add_alignment = ("INSERT INTO Alignments "
#                             "(almnt_id, prot_head) "
#                             "VALUES (%s, %s)")
#             data_alignment = (ali_id_counter, pbd_id)
#             cursor.execute(add_alignment, data_alignment)
#         
#         # increment this counter for each sup_fam
#         ali_id_counter += 1

def insert_ids_into_alignments(sup_fams: dict):
    """Matches the pdb_id to the ali_id and inserts them into the alignments table,
    meaning: 
        pdb_id_1, pbd_id_2, ... -> are in same sup_fam => ali_id_1
        pdb_id_3, pbd_id_4, ... -> are in same sup_fam => ali_id_2
        etc.
    In the sup_fams dict the pdb_ids are already grouped by sup_fam:
    sup_fams:= {sup_fam: [(pbd_id, ali_seq, sec_seq)*]}
    """

    ali_id_counter = 1
    for _, entries in sup_fams.items():
        for entry in entries:
            pbd_id, ali_seq, sec_seq = entry
            # insert pbd_id and ali_id into alignments
            # id (auto_increment), ali_id, prot_head
            add_alignment = ("INSERT INTO Alignments "
                            "(almnt_id, prot_head) "
                            "VALUES (%s, %s)")
            data_alignment = (ali_id_counter, pbd_id)
            cursor.execute(add_alignment, data_alignment)
        ali_id_counter += 1

def insert_alignments_into_sequences(sup_fams: dict, pdb_id_to_os_id: dict):
    """Inserts all alignments of type (sec_ali, seq_ali) into Sequences and checks if they are already in the db.
    Also matches the pdb_id to the os_id
    """

    for _, entries in sup_fams.items():
        for entry in entries:

            # get entry
            pbd_id, ali_seq, sec_seq = entry

            # check if ali_seq is alrady in sequences
            cursor.execute("SELECT source_id, type, sequence FROM Sequences WHERE source_id=%s AND type=%s AND sequence=%s", (pbd_id, "seq_ali", ali_seq))
            if cursor.fetchone():
                print(f"{pbd_id}: ali_seq already in sequences")
            else:
                # insert ali into sequences:
                # id (auto_increment), type=homstrad_alignment, sequence=ali_seq, source=HOMSTRAD, source_id=pdb_id, os_id
               
                no_chain_pbd_id = pbd_id
                if len(pbd_id) == 5:
                    no_chain_pbd_id = pbd_id[:-1]
                add_ali_seq = ("INSERT INTO Sequences "
                           "(type, sequence, source, source_id, os_id) "
                           "VALUES (%s, %s, %s, %s, %s)")
                data_seq = ("seq_ali", ali_seq, "homstrad", pbd_id, pdb_id_to_os_id[no_chain_pbd_id])
                cursor.execute(add_ali_seq, data_seq)
           
            # check if sec_seq is alrady in sequences
            cursor.execute("SELECT source_id, type FROM Sequences WHERE source_id=%s AND type=%s AND sequence=%s", (pbd_id, "sec_ali", sec_seq))
            if cursor.fetchone():
                print(f"{pbd_id}: sec_ali already in sequences")
            else:
                # insert sec into sequences
                no_chain_pbd_id = pbd_id
                if len(pbd_id) == 5:
                    no_chain_pbd_id = pbd_id[:-1]

                add_sec_seq = ("INSERT INTO Sequences "
                           "(type, sequence, source, source_id, os_id) "
                           "VALUES (%s, %s, %s, %s, %s)")
                data_seq = ("sec_ali", sec_seq, "homstrad", pbd_id, pdb_id_to_os_id[no_chain_pbd_id])
                cursor.execute(add_sec_seq, data_seq)
            
            # now check if entry got inserted
            # cursor.execute("SELECT * FROM Sequences WHERE source_id=%s", (pbd_id,))
            # if cursor.fetchone():
            #     print(f"{pbd_id}: entry got inserted")


if __name__ == "__main__":
    sup_fams, id_to_sup_fams, organism_dict = get_db("./HOMSTRAD/")
    # insert organisms
    #  WARN: only run once
    # insert_os(organism_dict)
    
    pdb_id_to_os_id = get_os_id_dict(organism_dict)

    # insert alignments into Sequences
    #  WARN: only run once
    # insert_alignments_into_sequences(sup_fams, pdb_id_to_os_id)






