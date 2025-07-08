fl = input()
open(
    f"{fl}_sorted.txt", 
    'w', 
    encoding="utf8").writelines([
        line + '\n' 
        for line in sorted(set([
            line.rstrip()
            for line in open(
                f"{fl}.txt",
                encoding="utf8")]))])