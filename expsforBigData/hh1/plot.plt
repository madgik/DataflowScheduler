set output 'hhExampleLigo.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "hhExample/LIGO/small" using 2:($1/60000) title 'small VMs' with points pt 8 lc rgb 'red',\
"hhExample/LIGO/large" using 2:($1/60000) title 'large VMs' with points pt 1 lc rgb 'blue',\
"hhExample/LIGO/hetero" using 2:($1/60000) title 'heterogeneous VMs' with points pt 2 lc rgb 'green'
set output 'others.eps'

set output 'hhExampleMontage.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "hhExample/MONTAGE/small" using 2:($1/60000) title 'small VMs' with points pt 8 lc rgb 'red',\
"hhExample/MONTAGE/large" using 2:($1/60000) title 'large VMs' with points pt 1 lc rgb 'blue',\
"hhExample/MONTAGE/hetero" using 2:($1/60000) title 'heterogeneous VMs' with points pt 2 lc rgb 'green'
set output 'others.eps'

set output 'hhTwoTypesExampleLigo.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "hhExample/LIGO/small" using 2:($1/60000) title 'small VMs' with points pt 8 lc rgb 'red',\
"hhExample/LIGO/large" using 2:($1/60000) title 'large VMs' with points pt 1 lc rgb 'blue',\
"hhExample/LIGO/twoHetero" using 2:($1/60000) title 'heterogeneous VMs' with points pt 2 lc rgb 'green'
set output 'others.eps'

set output 'hhTwoTypesExampleMontage.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (min)'
plot "hhExample/MONTAGE/small" using 2:($1/60000) title 'small VMs' with points pt 8 lc rgb 'red',\
"hhExample/MONTAGE/large" using 2:($1/60000) title 'large VMs' with points pt 1 lc rgb 'blue',\
"hhExample/MONTAGE/twoHetero" using 2:($1/60000) title 'heterogeneous VMs' with points pt 2 lc rgb 'green'
set output 'others.eps'