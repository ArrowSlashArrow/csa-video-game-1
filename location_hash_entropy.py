# checks hashes to make sure that their results are equally distributed

def location_hash(x, y): return x*x+x+y if x>=y else x+y*y

def compute_hash_entropy(m):
    def clue_hash(x, y): return ((location_hash(x, y) * 71) >> 5) % m
    hashes = { i: 0 for i in range(m)}

    wh = 1001
    ww = wh-1
    for i in range(wh):
        for j in range(ww):
            hashes[clue_hash(j, i)] = hashes[clue_hash(j, i)] + 1

    hv = [v for _, v in hashes.items()]
    avg = sum(hv) / len(hv)
    diff = max(avg - min(hv), max(hv) - avg)
    print(f"{m}: {int(avg)} +/- {diff:.3}")

for b in range (10): compute_hash_entropy(b + 1)

