reset
set terminal eps
set key font ",10" height 3
set xtics out font ",10"
set ytics font ",10" 
set xlabel font ",10" 
set ylabel font ",9"
set label font ",7"
set offset 0.05, 0.05, graph 0.25, 0.05
set xrange [*:*]
set yrange [*:*]
set pointsize 1.1



system "mkdir plotsMultiobj"

set output 'plotsMultiobj/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (min)'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMergeCP/Multiobj/ensemble.dat" using ($1):($2/60000) title 'dagMergeCP' with points pt 2 lc rgb 'blue',\
"dagMergeBR/Multiobj/ensemble.dat" using ($1):($2/60000) title 'dagMergeBR' with points pt 3 lc rgb 'green',\
"dagMergeBR/MultiobjIP/ensemble.dat" using ($1):($2/60000) title 'dagMergeBR IP' with points pt 1 lc rgb 'grey'
set output 'others.eps'

set output 'plotsMultiobj/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($7) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMergeCP/Multiobj/ensemble.dat" using ($1):($7) title 'dagMergeCP' with points pt 2 lc rgb 'blue',\
"dagMergeBR/Multiobj/ensemble.dat" using ($1):($7) title 'dagMergeBR' with points pt 3 lc rgb 'green',\
"dagMergeBR/MultiobjIP/ensemble.dat" using ($1):($7) title 'dagMergeBR IP' with points pt 1 lc rgb 'grey'
set output 'others.eps'





system "mkdir plotsComp"

set output 'plotsComp/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (min)'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMergeBR/Multiobj/ensemble.dat" using ($1):($2/60000) title 'dagMergeBR multiobjective' with points pt 3 lc rgb 'blue'
set output 'others.eps'

set output 'plotsComp/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($7) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMergeBR/Multiobj/ensemble.dat" using ($1):($7) title 'dagMergeBR multiobjective' with points pt 3 lc rgb 'blue'
set output 'others.eps'
