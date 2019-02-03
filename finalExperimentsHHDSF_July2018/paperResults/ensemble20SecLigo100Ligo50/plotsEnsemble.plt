reset
set terminal eps
set key font ",9" # height 3
set key right
set xtics out font ",8"
set ytics out font ",8" 
set ztics out font ",8" 
set xlabel font ",9" 
set ylabel font ",9"
set zlabel font ",9"
set label font ",9"
set offset 0.05, 0.05, graph 0.25, 0.05
set xrange [*:*]
set yrange [*:*]
set zrange [*:*]
set pointsize 0.7
set ticslevel 0
set view 70,30


system "mkdir plotsComp"

set output 'plotsComp/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Ex. Time (min)'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($2/60000) title 'dagMerge multiobjective' with points pt 3 lc rgb 'blue',\
"Moheft/ensemble.dat" using ($1):($2/60000) title 'Moheft biobjective (commonEntry)' with points pt 4 lc rgb 'grey'
set output 'others.eps'

set output 'plotsComp/unfairnessNormToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($7) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($7) title 'dagMerge multiobjective' with points pt 3 lc rgb 'blue',\
"Moheft/ensemble.dat" using ($1):($7) title 'Moheft biobjective (commonEntry)' with points pt 4 lc rgb 'grey'
set output 'others.eps'

set output 'plotsComp/avgstretch.eps'
set xlabel 'Total Money ($)'
set ylabel 'Stretch'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($4) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($4) title 'dagMerge multiobjective' with points pt 3 lc rgb 'blue',\
"Moheft/ensemble.dat" using ($1):($4) title 'Moheft biobjective (commonEntry)' with points pt 4 lc rgb 'grey'
set output 'others.eps'

set output 'plotsComp/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($6) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($6) title 'dagMerge multiobjective' with points pt 3 lc rgb 'blue',\
"Moheft/ensemble.dat" using ($1):($6) title 'Moheft biobjective (commonEntry)' with points pt 4 lc rgb 'grey'
set output 'others.eps'

system "mkdir splot"

set grid x y z
set hidden3d
set output 'splot/stretch3D.eps'
set xlabel 'Total Money ($)'
set zlabel 'mean Stretch'
set ylabel 'Ex. Time (min)'
splot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000):($4) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($2/60000):($4) title 'dagMerge multiobjective' with points pt 3 lc rgb 'blue',\
"Moheft/ensemble.dat" using ($1):($2/60000):($4) title 'Moheft biobjective (commonEntry)' with points pt 4 lc rgb 'grey'
set output 'others.eps'

set grid x y z
set hidden3d
set output 'splot/unfairness3D.eps'
set xlabel 'Total Money ($)'
set zlabel 'Unfairness'
set ylabel 'Ex. Time (min)'
splot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000):($6) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($2/60000):($6) title 'dagMerge multiobjective' with points pt 3 lc rgb 'blue',\
"Moheft/ensemble.dat" using ($1):($2/60000):($6) title 'Moheft biobjective (commonEntry)' with points pt 4 lc rgb 'grey'
set output 'others.eps'

set grid x y z
set hidden3d
set output 'splot/unfairnessNorm3D.eps'
set xlabel 'Total Money ($)'
set zlabel 'Unfairness'
set ylabel 'Ex. Time (min)'
splot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000):($7) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($2/60000):($7) title 'dagMerge multiobjective' with points pt 3 lc rgb 'blue',\
"Moheft/ensemble.dat" using ($1):($2/60000):($7) title 'Moheft biobjective (commonEntry)' with points pt 4 lc rgb 'grey'
set output 'others.eps'
unset hidden3d
