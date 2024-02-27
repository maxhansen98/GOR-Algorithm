import argparse


def load_sequence_from_fasta(fasta_file):
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


def search_k_mer(k: int, seq: str):
    for i in range(len(seq)-k):
        kmer = seq[i:i+k]
        print(kmer)


def get_uniq_bases(list_k: int, fasta_path:str):
    fasta_seqs = load_sequence_from_fasta(fasta_path)
    search_k_mer(3, fas)
    


if __name__ == "__main__":
    # create parser
    parser = argparse.ArgumentParser(description="description")
    parser.add_argument('--fasta', type=str, required=True)
    parser.add_argument('--k', nargs="+", type=int, required=True)
    args = parser.parse_args()

    get_uniq_bases(args.k, args.fasta)

    # main()
