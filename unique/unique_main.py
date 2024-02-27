#!/usr/bin/python3

import argparse
from collections import defaultdict
import matplotlib.pyplot as plt


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


def get_kmers(k, seqs_dict, s_pos):
    """
    get a list containing tuples of (seq_id, list[kmers]) 
    and a dict containing counts of all kmers
    if s_pos was set, it only appends kmers per seq if the index matches
    """

    kermers_per_id = []
    global_kmer_dict = defaultdict(set)
    for id, seq in seqs_dict.items():
        per_seq_kmers = []
        for i in range(len(seq)-k+1):
            kmer = seq[i:i+k]
            if s_pos == -1 or i == s_pos:
                global_kmer_dict[kmer].add(id)
                per_seq_kmers.append(kmer)
        
        kermers_per_id.append((id, per_seq_kmers))
    
    return global_kmer_dict, kermers_per_id


def plot_results(k, unique_count, fasta_seqs_dict):
    total_uniques=0
    for kmer_count in fasta_seqs_dict.values():
        print(kmer_count)
        if kmer_count == 1:
            total_uniques+=1
    print(total_uniques)


def main(list_k , fasta_path, s_pos):
    fasta_seqs_dict = load_sequence_from_fasta(fasta_path)
    for k in list_k:
        global_kmer_dict, kmers_per_seq = get_kmers(k, fasta_seqs_dict, s_pos)
        unique_count  = 0
        for _, kmers in kmers_per_seq:  # get kmers per seq
            for kmer in kmers:  # iter over each of them
                if len(global_kmer_dict[kmer]) == 1:
                    unique_count+=1
                    break

        print(f"{k}\t{unique_count}")

        # plot_results(list_k, unique_count, global_kmer_dict)


if __name__ == "__main__":
    # create parser
    parser = argparse.ArgumentParser(description="description")
    parser.add_argument('--fasta', type=str, required=True)
    parser.add_argument('--k', nargs="+", type=int, required=True)
    parser.add_argument('--start', type=int,default=-1, required=False)
    args = parser.parse_args()

    main(args.k, args.fasta, args.start)
    

    # ein dict (das "fasta_dict") mit : {fasta_id : sequenz von dieser ID}
    # ein default dict: {kmer: {fasta_ids}}
    # from collections import defaultdict
    # global_kmer_dict = defaultdict(set)
    #
    # iteriere über alle einträte in dem "fasta_dict":
    # for id, seq in seqs_dict.items():
    # ... hier kommt dann ein weiterer for loop,
    # in dem du kmer=seq[i:i+k] machst für i<= len(seq)-1
    # alle diese kmers werden dann ins default dict hinzugefügt mit
    # ... global_kmer_dict[kmer].add(id)
