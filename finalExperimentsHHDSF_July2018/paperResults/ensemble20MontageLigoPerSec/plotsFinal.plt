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


system "mkdir plotsFinal"

set output 'plotsFinal/skylinePruning.eps'
set xlabel 'Total Money ($)'
set ylabel 'Ex. Time (min)'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000) title 'HHDS: biObj commonEntry' with points pt 8 lc rgb 'black',\
"dagMerge/Biobj/ensemble.dat" using ($1):($2/60000) title 'biObj dagMerge' with points pt 3 lc rgb 'blue'
set output 'others.eps'

set output 'plotsFinal/unfairnessPruning.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($6) title 'HHDS: biObj commonEntry' with points pt 8 lc rgb 'black',\
"dagMerge/Biobj/ensemble.dat" using ($1):($6) title 'biObj dagMerge' with points pt 3 lc rgb 'blue'
set output 'others.eps'

set output 'plotsFinal/unfairnessNormPruning.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($7) title 'HHDS: biObj commonEntry' with points pt 8 lc rgb 'black',\
"commonEntry/Multiobj/ensemble.dat" using ($1):($7) title 'multiObj commonEntry' with points pt 3 lc rgb 'blue'
set output 'others.eps'



set output 'plotsFinal/skylineRanking.eps'
set xlabel 'Total Money ($)'
set ylabel 'Ex. Time (min)'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000) title 'HHDS: biObj commonEntry' with points pt 8 lc rgb 'black',\
"commonEntry/Multiobj/ensemble.dat" using ($1):($2/60000) title 'multiObj commonEntry' with points pt 3 lc rgb 'blue'
set output 'others.eps'

set output 'plotsFinal/unfairnessRanking.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($6) title 'HHDS: biObj commonEntry' with points pt 8 lc rgb 'black',\
"commonEntry/Multiobj/ensemble.dat" using ($1):($6) title 'multiObj commonEntry' with points pt 3 lc rgb 'blue'
set output 'others.eps'

set output 'plotsFinal/unfairnessNormRanking.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/Biobj/ensemble.dat" using ($1):($7) title 'HHDS: biObj commonEntry' with points pt 8 lc rgb 'black',\
"commonEntry/Multiobj/ensemble.dat" using ($1):($7) title 'multiObj commonEntry' with points pt 3 lc rgb 'blue'
set output 'others.eps'


set grid x y z
set hidden3d
set output 'plotsFinal/unfairness3D.eps'
set xlabel 'Total Money ($)'
set zlabel 'Unfairness'
set ylabel 'Ex. Time (min)'
splot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000):($6) title 'HHDS: biObj commonEntry' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($2/60000):($6) title 'HHDS-F: multiObj dagMerge' with points pt 3 lc rgb 'blue'
set output 'others.eps'

set grid x y z
set hidden3d
set output 'plotsFinal/unfairnessNorm3D.eps'
set xlabel 'Total Money ($)'
set zlabel 'Unfairness'
set ylabel 'Ex. Time (min)'
splot "commonEntry/Biobj/ensemble.dat" using ($1):($2/60000):($7) title 'HHDS: biObj commonEntry' with points pt 8 lc rgb 'black',\
"dagMerge/Multiobj/ensemble.dat" using ($1):($2/60000):($7) title 'HHDS-F: multiObj dagMerge' with points pt 3 lc rgb 'blue'
set output 'others.eps'
unset hidden3d


