wereables-pebble
================

Z racji ograniczeń Pebbla co do pamięci przeznaczonej na aplikację, na dane pobierane ze smartfona pozostaje ok. 6kB. W związku z tym trzeba się ograniczać do pewnej ilości obiektów.
Szacowane średnie rozmiary obiektów (wszystko zależy od długości stringów ich nazw):
- lokacja ~45B
- pracownik ~45B
- achievement ~55B

Dla oszczędności pamięci przechowywane są jednocześnie lokacje oraz wymiennie albo pracownicy albo achievementy.
Przykładowo w tych dostępnych 6kB powinno się zmieścić 40 lokacji + (90 pracowników albo 70 achievementów)
