#!/usr/bin/python3
# Programmierpraktikum WS2023/2024
# Uebungsblatt 2, Aufgabe 14 (HOMSTRAD)
# Malte A. Weyrich

import os
from collections import defaultdict
import mysql.connector

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
    

    # iterate over each super family (sup_fam)
    for curr_sup_fam in subfamily_dirs:
        path_to_dir = os.path.join(path_to_homstrad, curr_sup_fam)
        files = os.listdir(path_to_dir)
        
        tem_file = [file for file in files if file[-1] == "m"]
        # get alignments
        tem_file = os.path.join(path_to_dir, tem_file[0])
        print(tem_file)
        
        content_tem=[]

        # read .tem
        with open(tem_file) as tem:
            content_tem = tem.readlines()

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
    
        # append all relevant vals to sup_fam_dict
        for sup_fam_pbd_id_tup in ali_seq_dict.keys():
            sup_fam_entry = (sup_fam_pbd_id_tup[1], ali_seq_dict[sup_fam_pbd_id_tup], sec_seq_dict[sup_fam_pbd_id_tup])
            sup_fams[sup_fam_pbd_id_tup[0]].add(sup_fam_entry)
    

    print()
    for key, val in sup_fams.items():
        print("########################################################################################################################################################################################################################################################################################")
        print(key)
        for v in val:
            print(v)
        print("############################################################################################################################################")
        print()
    # print(sup_fams)
    # print(id_to_sup_fams)

def insert_into_db(sup_fams: dict, id_to_sup_fams: dict):

    pass

if __name__ == "__main__":
   get_db("./HOMSTRAD/")

