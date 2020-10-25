Clasa MatrixFilter primeste args de la Main

1) Se parseaza argumentele primite
2) Se creaza si se populeaza un fisier cu date daca acesta nu exista pentru imagine si pentru filtru
3) Se aplica padding pentru inmultirea cu filtrul
4) Se efectueaza aplicarea filtrului secvential
5) Se efectueaza aplicarea filturlui paralel
6) Se compara timpii
7) Se afiseaza ca output

Pentru specificatiile 4 threaduri obtin cei mai buni timpi.
Cazuri testare:
    1) 10 x 10000:
        - Secvential: 351.43 MS
        - Paralel: 101.80 MS
    2) 10000 x 10:
        - Secvential: 352.79 MS
        - Paralel: 97.63 MS
    3) 1000 x 1000:
        - Secvential: 2407.95 MS
        - Paralel: 883.465 MS   
