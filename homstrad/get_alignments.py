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


    


cnx = mysql.connector.connect(user='bioprakt3', password='$1$dXmWsf6J$rQWMUrRzyAhhqjPscdRbG.',
                              host='mysql2-ext.bio.ifi.lmu.de',
                              database='bioprakt3',
                              port='3306')
cursor = cnx.cursor()

parser = argparse.ArgumentParser()
parser.add_argument('--pdb', type=str, help='Pdb(s) of Sequence', nargs='+', required=True)
args = parser.parse_args()

if args.pdb:
    for pdb in args.pdb:
        print(get_alignments(pdb))
       
    