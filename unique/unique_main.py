import argparse


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


def get_all_kmers(k: int, seqs_dict: dict):
    """
    get a list containing tupels of (seq_id, list[kmers]) 
    """
    all_kmers = []
    for id, seq in seqs_dict.items():
        per_seq_kmers = set()
        for i in range(len(seq)-k+1):
            kmer = seq[i:i+k]
            per_seq_kmers.add(kmer)
        all_kmers.append((id, per_seq_kmers))

    # for id, li in all_kmers:
    #     print(len(li))
    
    print(len(all_kmers))

    return all_kmers


def search_all_kmers(kmer_list: list[tuple[str, list[str]]]):
    """
    searches in all lists of kmers for matches except for the seq of origin
    """
    total_count = 0
    for target_entry in kmer_list:
        # get new target candidate
        target_id = target_entry[0]
        target_kmers = target_entry[1]

        uniq_gene_counter = 0  # reset counter

        for target_kmer in target_kmers:
            uniq_gene_counter = 0
            for search_entry in kmer_list:
                # check if were not in the seq of origin 
                if search_entry[0] != target_id:
                    for search_kmer in search_entry[1]:
                        if target_kmer == search_kmer:
                            uniq_gene_counter += 1
        if uniq_gene_counter == 0:
            print("Seq found in ")
            total_count+=1


        print(f"{len(kmer_list[0])}\t")
        break

            



def get_uniq_bases(list_k: list[int], fasta_path:str):
    fasta_seqs = load_sequence_from_fasta(fasta_path)
    for k in list_k:
        all_kmers = get_all_kmers(k, fasta_seqs)
        search_all_kmers(all_kmers)



if __name__ == "__main__":
    # create parser
    parser = argparse.ArgumentParser(description="description")
    parser.add_argument('--fasta', type=str, required=True)
    parser.add_argument('--k', nargs="+", type=int, required=True)
    args = parser.parse_args()

    get_uniq_bases(args.k, args.fasta)

    # main()
