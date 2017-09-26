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
set pointsize 0.8

system "mkdir plots"



set output 'plots/minSubdagTime.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (Normalized)'
plot "commonEntry/ensemble4.txt" using ($1):($5) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble4.txt" using ($1):($5) title 'userPreference' with points pt 8 lc rgb 'green',\
"sizeBased/ensemble4.txt" using ($1):($5) title 'sizeBased' with points pt 8 lc rgb 'red'
set output 'plots/others.eps'


set output 'plots/skylineSubDag.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "commonEntry/ensemble4.txt" using ($3):($4) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble4.txt" using ($3):($4) title 'userPreference' with points pt 8 lc rgb 'green',\
"sizeBased/ensemble4.txt" using ($3):($4) title 'sizeBased' with points pt 8 lc rgb 'red'
set output 'plots/others.eps'


set output 'plots/skyline.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "commonEntry/ensemble4.txt" using ($1):($2) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble4.txt" using ($1):($2) title 'userPreference' with points pt 8 lc rgb 'green',\
"sizeBased/ensemble4.txt" using ($1):($2) title 'sizeBased' with points pt 8 lc rgb 'red'
set output 'plots/others.eps'


set output 'plots/meanSubdagTime.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time  (Normalized)'
plot "commonEntry/ensemble4.txt" using ($1):($4) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble4.txt" using ($1):($4) title 'userPreference' with points pt 8 lc rgb 'green',\
"sizeBased/ensemble4.txt" using ($1):($4) title 'sizeBased' with points pt 8 lc rgb 'red'

set output 'plots/others.eps'

set output 'plots/unfairnessMetric.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time (s)'
plot "commonEntry/ensemble4.txt" using ($1):($7) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble4.txt" using ($1):($7) title 'userPreference' with points pt 8 lc rgb 'green',\
"sizeBased/ensemble4.txt" using ($1):($7) title 'sizeBased' with points pt 8 lc rgb 'red'
set output 'plots/others.eps'


set output 'plots/maxSubdagTime.eps'
set xlabel 'Cost ($)'
set ylabel 'Execution Time  (Normalized)'
plot "commonEntry/ensemble4.txt" using ($1):($6) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble4.txt" using ($1):($6) title 'userPreference' with points pt 8 lc rgb 'green',\
"sizeBased/ensemble4.txt" using ($1):($6) title 'sizeBased' with points pt 8 lc rgb 'red'
set output 'plots/others.eps'

#set output 'plots/single.eps'
#set xlabel 'Cost ($)'
#set ylabel 'Execution Time (s)'
#plot "commonEntry/single1.txt" using ($1):($3) title 'commonEntry' with points pt 8 lc rgb 'blue',\
#"userPref/single1.txt" using ($1):($3) title 'userPreference' with points pt 8 lc rgb 'green'
set output 'plots/others.eps'
