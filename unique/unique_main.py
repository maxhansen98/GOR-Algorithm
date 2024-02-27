#!/usr/bin/python3

import argparse
from collections import defaultdict


def load_sequence_from_fasta(fasta_file):
    """
    read in the genome fasta
    """
    sequences = {}
    current_id = ''
    with open(fasta_file, 'r') as file:
        for line in file:
            if line.startswith('>'):  # Header line
                current_id = line.strip()[1:]
                sequences[current_id] = ''
            else:  # Sequence line
                sequences[current_id] += line.strip()
    return sequences


def get_kmers(k, seqs_dict):
    """
    get a list containing tupels of (seq_id, list[kmers]) 
    and a dict containing counts of all kmers
    """
    kermers_per_id = []
    global_kmer_dict = defaultdict(int)
    for id, seq in seqs_dict.items():
        per_seq_kmers = set()
        for i in range(len(seq)-k+1):
            kmer = seq[i:i+k]
            per_seq_kmers.add(kmer)
            global_kmer_dict[kmer] += 1
        kermers_per_id.append((id, per_seq_kmers))

    return global_kmer_dict, kermers_per_id


def get_uniq_bases(list_k , fasta_path):
    fasta_seqs_dict = load_sequence_from_fasta(fasta_path)
    for k in list_k:
        global_kmer_dict, kmers_per_seq = get_kmers(k, fasta_seqs_dict)
        unique_count  = 0
        for seq_id, kmers in kmers_per_seq:
            for kmer in kmers:
                if global_kmer_dict[kmer] == 1:
                    # print(seq_id, kmer)
                    unique_count+=1
                    break
        print(f"{k}\t{unique_count}")


if __name__ == "__main__":
    # create parser
    parser = argparse.ArgumentParser(description="description")
    parser.add_argument('--fasta', type=str, required=True)
    parser.add_argument('--k', nargs="+", type=int, required=True)
    args = parser.parse_args()

    get_uniq_bases(args.k, args.fasta)

