#!/usr/bin/python3
import mysql.connector
from mysql.connector import errorcode
import argparse

def get_alignments(pdb):
    # get alignmentif for a given pdb
    res_alignments = []
    cursor.execute("select almnt_id from Alignments where prot_head = '2cro';")
    alignments = cursor.fetchall()
    for alignment in alignments:
        res_alignments.append({alignment[0]:{}})
        cursor.execute("select prot_head from Alignments where almnt_id = %s;", (alignment[0],))
        proteins = cursor.fetchall()
        for protein in proteins:
            cursor.execute("select sequence from Sequences where source_id = %s and source = %s and type = %s;", (protein[0],'homstrad','seq_ali'))
            sequence = cursor.fetchall()
            res_alignments[-1][alignment[0]][protein[0]] = sequence[0][0]
        
        
    return res_alignments

def get_alignment(pdb):
    alignment_ids = {}
    res_alignments = []
    for i in range(len(pdb)):
        cursor.execute("select almnt_id from Alignments where prot_head = %s;", (pdb[i],))
        alignment = cursor.fetchall()
        for al in alignment:
            if al[0] not in alignment_ids:
                alignment_ids[al[0]] = [pdb[i]]
            else:
                alignment_ids[al[0]].append(pdb[i])
    
    #alignment_ids = sorted(alignment_ids, key=lambda x: len(x), reverse=True)
    proteins = list(alignment_ids.values())[0]
    a_id = list(alignment_ids.keys())[0]
    if len(alignment_ids.keys()) == 1 and len(proteins) == len(pdb):
        res_alignments.append({a_id:{}})
        for protein in proteins:
            print(protein)
            cursor.execute("select sequence from Sequences where source_id = %s and source = %s and type = %s;", (protein,'homstrad','seq_ali'))
            sequence = cursor.fetchall()
            print(sequence)
            res_alignments[-1][a_id][protein] = sequence[0][0]
    
        return res_alignments
        

    
   

    


cnx = mysql.connector.connect(user='bioprakt3', password='$1$dXmWsf6J$rQWMUrRzyAhhqjPscdRbG.',
                              host='mysql2-ext.bio.ifi.lmu.de',
                              database='bioprakt3',
                              port='3306')
cursor = cnx.cursor()

parser = argparse.ArgumentParser()
parser.add_argument('--pdb', type=str, help='Pdb(s) of Sequence', nargs='+', required=True)
args = parser.parse_args()

if args.pdb:
    if len(args.pdb) == 1:
        print(get_alignments(args.pdb))
    elif len(args.pdb) > 1:
        print(get_alignment(args.pdb))

       
    