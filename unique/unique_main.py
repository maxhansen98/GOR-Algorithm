#!/usr/bin/python3

import argparse
from collections import defaultdict
import matplotlib.pyplot as plt
import seaborn as sns


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


def plot_percentages(k_values, unique_counts, total_genes):
    percentages = [count / total_genes * 100 for count in unique_counts]

    sns.set_theme()
    sns.set_palette("colorblind")
    sns.set_theme("paper")
    sns.set_style("ticks")

    # Create the bar plot with custom error bars and hue="Model"
    plt.figure(figsize=(14, 10))
    ax = sns.barplot(x=k_values, y=percentages, edgecolor='black')

    # Customize the plot labels
    ax.set_xlabel("k", fontsize=20)
    ax.set_ylabel('Percentage of unique genes (%)', fontsize=20)
    ax.set_title('Percentage of unique genes for different k values', fontsize=20)
    ax.tick_params(axis='both', which='major', labelsize=17)
    sns.despine()
    plt.show()


def minimum_uniq_seq(fasta_seqs_dict: dict):
    
    
    check_dict = {}  # init check_dict
    for id, _ in fasta_seqs_dict.items():
        check_dict[id] = -1  # start val = -1
    
    k = 1  # init k
    start_pos = 0  # set start pos to be 0

    # while there are non uniques, increase k and check for new uniques
    # ignore genes that are already unique for a given k
    while -1 in check_dict.values():
        # for each seq, as soon as k is longer than a seq, we will set its
        # count to 0
        for seq_id, seq in fasta_seqs_dict.items():
            if k > len(seq):
                check_dict[seq_id] = 0

        global_kmer_dict, kmers_per_seq = get_kmers(k, fasta_seqs_dict, start_pos)
        
        for seq_id, kmers in kmers_per_seq:  # get kmers per seq
            if check_dict[seq_id] == -1:
                for kmer in kmers:  # iter over each of them
                    if len(global_kmer_dict[kmer]) == 1:
                        check_dict[seq_id] = k
                        fasta_seqs_dict.pop(seq_id)  # remove identified gene
                        break
        k+=1

        
    sns.set_theme()
    sns.set_palette("colorblind")
    sns.set_theme("paper")
    sns.set_style("ticks")

    # Create the bar plot with custom error bars and hue="Model"
    plt.figure(figsize=(14, 10))
    ax = sns.barplot(x=list(check_dict.keys()), y=list(check_dict.values()), edgecolor='black')

    # Customize the plot labels
    ax.set_xlabel("Fasta ids", fontsize=20)
    ax.set_ylabel('Minimum length needed for explicitly of genes', fontsize=20)
    ax.set_xticklabels([])
    ax.set_title('Minimum unique starting sequences', fontsize=20)
    ax.tick_params(axis='both', which='major', labelsize=17)
    sns.despine()
    plt.show()


def main(list_k , fasta_path, s_pos, plot_ks, plot_uniqs):
    fasta_seqs_dict = load_sequence_from_fasta(fasta_path)
    unique_counts = []
    for k in list_k:
        global_kmer_dict, kmers_per_seq = get_kmers(k, fasta_seqs_dict, s_pos)
        unique_count  = 0
        for _, kmers in kmers_per_seq:  # get kmers per seq
            for kmer in kmers:  # iter over each of them
                if len(global_kmer_dict[kmer]) == 1:
                    unique_count+=1
                    break

        print(f"{k}\t{unique_count}")
        unique_counts.append(unique_count)

    # Plotting
    if plot_ks:
        plot_percentages(list_k, unique_counts, len(fasta_seqs_dict.keys()))
    if plot_uniqs:
        minimum_uniq_seq(fasta_seqs_dict) 


if __name__ == "__main__":
    # create parser
    parser = argparse.ArgumentParser(description="description")
    parser.add_argument('--fasta', type=str, required=True)
    parser.add_argument('--k', nargs="+", type=int, required=True)
    parser.add_argument('--start', type=int,default=-1, required=False)
    parser.add_argument('--res', action='store_true', help="Description of --res flag")
    parser.add_argument('--uniq', action='store_true', help="Description of --uniq flag")

    args = parser.parse_args()

    main(args.k, args.fasta, args.start, args.res, args.uniq)
    
