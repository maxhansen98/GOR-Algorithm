#!/usr/bin/python3
# Gruppe 03
# ProgPra WS2324

import argparse
import glob
import os

import requests

AA_POS = (17, 20)
POSITION = (22, 26)
HELIX_SEQ_START = (21, 25)
HELIX_SEQ_END = (33, 37)
HELIX_CHAIN = 19
SHEET_SEQ_START = (22, 26)
SHEET_SEQ_END = (33, 37)
SHEET_CHAIN = 21
WINDOW_SIZE = 17
DSSP_AA = 13
DSSP_SS = 16

AA_DICT = {
    "ALA": "A", "ARG": "R", "ASN": "N", "ASP": "D", "CYS": "C",
    "GLU": "E", "GLN": "Q", "GLY": "G", "HIS": "H", "ILE": "I",
    "LEU": "L", "LYS": "K", "MET": "M", "PHE": "F", "PRO": "P",
    "SER": "S", "THR": "T", "TRP": "W", "TYR": "Y", "VAL": "V"
}


def get_pdb_file(id):
    """
    Download ptb_file from rcsb
    """
    url = f'https://files.rcsb.org/download/{id}.pdb'
    response = requests.get(url)
    file_path = f"{id}.pdb"
    with open(file_path, 'w') as file:
        file.write(response.text)
    return file_path


def get_pdb_file_info(id_path):
    """
    Parse PDB File
    Extract Atom information
    :return: Dict with the positions of the atoms as keys
    """
    counter = 0
    atom_info = {}
    ss_info = {}
    with open(id_path, 'r') as pdb_file:
        for line in pdb_file:
            if line.startswith('HELIX'):  # Get Secondary Structure-Infos for Helix
                helix_start = int(line[HELIX_SEQ_START[0]:HELIX_SEQ_START[1]].strip())
                helix_end = int(line[HELIX_SEQ_END[0]:HELIX_SEQ_END[1]].strip())
                ss_info[helix_start, helix_end] = {'H'}
            if line.startswith('SHEET'):  # Get Secondary Structure-Infos for Sheet, else C
                sheet_start = int(line[SHEET_SEQ_START[0]:SHEET_SEQ_START[1]].strip())
                sheet_end = int(line[SHEET_SEQ_END[0]:SHEET_SEQ_END[1]].strip())
                ss_info[sheet_start, sheet_end] = {'E'}
            if line.startswith('ATOM'):  # Extract Atom Info
                position = int(line[POSITION[0]:POSITION[1]].strip())
                aa = line[AA_POS[0]:AA_POS[1]].strip()
                atom_info[position] = aa
            if line.startswith('MODEL'):
                counter += 1
                if counter == 2:
                    break
    return atom_info, ss_info


def get_sec_struct(ss_info, value):
    """
    :param ss_info with keys (chain name, start_pos, end_pos)
    :param atom_info of current position
    :return: current secondary structure
    """
    for key in ss_info:
        if key[0] <= value <= key[1]:
            if ss_info[key] == {'H'}:
                return 'H'
            else:
                return 'E'
    return 'C'


def get_aass(aa_info, ss_info):
    aa_seq = ''
    ss_seq = ''

    for key in aa_info:
        aa = aa_info[key]
        aa_seq += AA_DICT.get(aa, 'X')

        ss_seq += get_sec_struct(ss_info, key)
    return aa_seq, ss_seq


def write_aass(aa_seq, ss_seq, output, id):
    output.write(f">{id}\n")
    output.write(f"AS {aa_seq}\n")
    output.write(f"SS {ss_seq}\n\n")
    pass


def get_dssp_file_info(dssp_file):
    aa_seq = ''
    ss_seq = ''
    line_reading = False
    with open(dssp_file, 'r') as file:
        for line in file:
            if line.startswith('  #'):
                line_reading = True
                continue
            if line_reading:
                if line[DSSP_AA] == '!':
                    continue
                aa_seq += line[DSSP_AA]
                if line[DSSP_SS] == 'E':
                    ss_seq += 'E'
                elif line[DSSP_SS] == 'H':
                    ss_seq += 'H'
                else:
                    ss_seq += 'C'
    return aa_seq, ss_seq

def main():
    parser = argparse.ArgumentParser(description='Process PDB files and create training sets')
    parser.add_argument('--file_path', type=str, nargs='+', help='Path to the PDB file to process')
    parser.add_argument('--pdb_id', type=str, nargs='+', help='PDB ID of the file to process')
    args = parser.parse_args()

    pdb_files = []
    pdb_ids = []
    dssp_files = []
    dssp_ids = []

    # File-input:
    if args.file_path:
        for path in args.file_path:
            if os.path.isfile(path):
                if os.path.splitext(path)[1] == '.pdb':  # PDB Files
                    pdb_files.append(path)
                    pdb_ids.append(os.path.splitext(os.path.basename(path))[0])
                elif os.path.splitext(path)[1] == '.dssp':  # DSSP Files
                    dssp_files.append(path)
                    dssp_ids.append(os.path.splitext(os.path.basename(path))[0])
            elif os.path.isdir(path):
                pdb_files.extend(glob.glob(os.path.join(path, '*.pdb')))  # PDB in Directory
                pdb_ids.extend([os.path.splitext(os.path.basename(file))[0] for file in pdb_files])
                dssp_files.extend(glob.glob(os.path.join(path, '*.dssp')))  # DSSP in Directory
                dssp_ids.extend([os.path.splitext(os.path.basename(file))[0] for file in dssp_files])
    # ID-Download-Input
    if args.pdb_id:  # Download PDB
        pdb_files.extend([get_pdb_file(pdb_id) for pdb_id in args.pdb_id])
        pdb_ids.extend(args.pdb_id)

    with open("seclib_file.db", 'w') as output:
        for index, pdb_file in enumerate(pdb_files):
            aa_info, ss_info = get_pdb_file_info(pdb_file)
            aa_seq, ss_seq = get_aass(aa_info, ss_info)
            if len(aa_seq) < WINDOW_SIZE: continue
            write_aass(aa_seq, ss_seq, output, pdb_ids[index])
        for index, dssp_file in enumerate(dssp_files):
            aa_seq, ss_seq = get_dssp_file_info(dssp_file)
            if len(aa_seq) < WINDOW_SIZE: continue
            write_aass(aa_seq, ss_seq, output, dssp_ids[index])
            


if __name__ == '__main__':
    main()
