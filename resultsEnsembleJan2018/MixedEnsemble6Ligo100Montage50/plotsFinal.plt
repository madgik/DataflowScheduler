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


system "mkdir plotsFinal"

set output 'plotsFinal/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (min)'
plot "commonEntry/BiObj/ensemble.dat" using ($1):($2/60000) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"commonEntry/BiObjU/ensemble.dat" using ($1):($2/60000) title 'minUnfairness (commonEntry)' with points pt 2 lc rgb 'black',\
"dagMerge/BiObj/ensemble.dat" using ($1):($2/60000) title 'minUtilization - rankedOps' with points pt 8 lc rgb 'grey',\
"dagMerge/BiObjU/ensemble.dat" using ($1):($2/60000) title 'minUnfairness - rankedOps' with points pt 2 lc rgb 'grey',\
"dagMerge/MultiObjMD/ensemble.dat" using ($1):($2/60000) title 'multiObjectiveMD - rankedOps' with points pt 2 lc rgb 'blue'
set output 'others.eps'

set output 'plotsFinal/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/BiObj/ensemble.dat" using ($1):($7) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"commonEntry/BiObjU/ensemble.dat" using ($1):($7) title 'minUnfairness (commonEntry)' with points pt 2 lc rgb 'black',\
"dagMerge/BiObj/ensemble.dat" using ($1):($7) title 'minUtilization - rankedOps' with points pt 8 lc rgb 'grey',\
"dagMerge/BiObjU/ensemble.dat" using ($1):($7) title 'minUnfairness - rankedOps' with points pt 2 lc rgb 'grey',\
"dagMerge/MultiObjMD/ensemble.dat" using ($1):($7) title 'multiObjectiveMD - rankedOps' with points pt 2 lc rgb 'blue'
set output 'others.eps'

system "mkdir plotsCommonEntry"

set output 'plotsCommonEntry/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (min)'
plot "commonEntry/BiObj/ensemble.dat" using ($1):($2/60000) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"commonEntry/BiObjU/ensemble.dat" using ($1):($2/60000) title 'minUnfairness (commonEntry)' with points pt 2 lc rgb 'black'
set output 'others.eps'

set output 'plotsCommonEntry/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/BiObj/ensemble.dat" using ($1):($7) title 'HHDS: minUtilization (commonEntry)' with points pt 8 lc rgb 'black',\
"commonEntry/BiObjU/ensemble.dat" using ($1):($7) title 'minUnfairness (commonEntry)' with points pt 2 lc rgb 'black'
set output 'others.eps'


system "mkdir plotsDagMerge"

set output 'plotsDagMerge/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (min)'
plot "dagMerge/BiObj/ensemble.dat" using ($1):($2/60000) title 'minUtilization - rankedOps' with points pt 8 lc rgb 'grey',\
"dagMerge/BiObjU/ensemble.dat" using ($1):($2/60000) title 'minUnfairness - rankedOps' with points pt 2 lc rgb 'grey',\
"dagMerge/MultiObjMD/ensemble.dat" using ($1):($2/60000) title 'multiObjectiveMD - rankedOps' with points pt 8 lc rgb 'blue'
set output 'others.eps'

set output 'plotsDagMerge/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "dagMerge/BiObj/ensemble.dat" using ($1):($7) title 'minUtilization - rankedOps' with points pt 8 lc rgb 'grey',\
"dagMerge/BiObjU/ensemble.dat" using ($1):($7) title 'minUnfairness - rankedOps' with points pt 2 lc rgb 'grey',\
"dagMerge/MultiObjMD/ensemble.dat" using ($1):($7) title 'multiObjectiveMD - rankedOps' with points pt 8 lc rgb 'blue'
set output 'others.eps'

system "mkdir plotsVS"

set output 'plotsVS/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (min)'
plot "commonEntry/BiObj/ensemble.dat" using ($1):($2/60000) title 'minUtilization (commonEntry)' with points pt 2 lc rgb 'black',\
"dagMerge/MultiObjMD/ensemble.dat" using ($1):($2/60000) title 'multiObjectiveMD - rankedOps' with points pt 8 lc rgb 'blue',\
"commonEntry/MultiObjMD/ensemble.dat" using ($1):($2/60000) title 'multiObjectiveMD (commonEntry)' with points pt 8 lc rgb 'grey'
set output 'others.eps'

set output 'plotsVS/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/BiObj/ensemble.dat" using ($1):($7) title 'minUtilization (commonEntry)' with points pt 2 lc rgb 'black',\
"dagMerge/MultiObjMD/ensemble.dat" using ($1):($7) title 'multiObjectiveMD - rankedOps' with points pt 8 lc rgb 'blue',\
"commonEntry/MultiObjMD/ensemble.dat" using ($1):($7) title 'multiObjectiveMD (commonEntry)' with points pt 8 lc rgb 'grey'
set output 'others.eps'
