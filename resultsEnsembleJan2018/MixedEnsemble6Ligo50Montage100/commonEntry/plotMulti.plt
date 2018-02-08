reset
set terminal eps
set key font ",15" height 3
set xtics out font ",15"
set ytics font ",15" 
set xlabel font ",15" 
set ylabel font ",15" offset -1
set label font ",4"
set offset 0, 0.05, graph 0.25, 0.0
set xrange [*:*]
set yrange [*:*]
set pointsize 1.2

system "mkdir plotsMulti"



set output 'plotsMulti/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time'
plot "MultiObjIP/ensemble.dat" using ($1):($2/60000) title 'MultiObj' with points pt 8 lc rgb 'blue',\
"MultiObjYC/ensemble.dat" using ($1):($2/60000) title 'MultiObjYC' with points pt 2 lc rgb 'green',\
"BiObj/ensemble.dat" using ($1):($2/60000) title 'BiObj' with points pt 2 lc rgb 'red'
set output 'plots/others.eps'


set output 'plotsMulti/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "MultiObjIP/ensemble.dat" using ($1):($7) title 'MultiObj' with points pt 8 lc rgb 'blue',\
"MultiObjYC/ensemble.dat" using ($1):($7) title 'MultiObjYC' with points pt 2 lc rgb 'green',\
"BiObj/ensemble.dat" using ($1):($7) title 'BiObj' with points pt 2 lc rgb 'red'
set output 'plots/others.eps'

