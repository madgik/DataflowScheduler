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

system "mkdir plots"



set output 'plots/minSubdagTime.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time'
plot "commonEntry/ensemble20.txt" using ($1):($5) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble20.txt" using ($1):($5) title 'userPreference' with points pt 2 lc rgb 'green',\
"slackByDag/ensemble20.txt" using ($1):($5) title 'slackByDag' with points pt 1 lc rgb 'red',\
"weightByDag/ensemble20.txt" using ($1):($5) title 'weightByDag' with points pt 1 lc rgb 'grey'
set output 'plots/others.eps'


set output 'plots/skylineSubDag.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (s)'
plot "commonEntry/ensemble20.txt" using ($3):($4) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble20.txt" using ($3):($4) title 'userPreference' with points pt 2 lc rgb 'green',\
"slackByDag/ensemble20.txt" using ($3):($4) title 'slackByDag' with points pt 1 lc rgb 'red',\
"weightByDag/ensemble20.txt" using ($3):($4) title 'weightByDag' with points pt 1 lc rgb 'grey'
set output 'plots/others.eps'


set output 'plots/skyline.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time (min)'
plot "commonEntry/ensemble20.txt" using ($1):($2/60000) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble20.txt" using ($1):($2/60000) title 'userPreference' with points pt 2 lc rgb 'green',\
"slackByDag/ensemble20.txt" using ($1):($2/60000) title 'slackByDag' with points pt 1 lc rgb 'red',\
"weightByDag/ensemble20.txt" using ($1):($2/60000) title 'weightByDag' with points pt 1 lc rgb 'grey'
set output 'plots/others.eps'


set output 'plots/meanSubdagTime.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time'
plot "commonEntry/ensemble20.txt" using ($1):($4) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble20.txt" using ($1):($4) title 'userPreference' with points pt 2 lc rgb 'green',\
"slackByDag/ensemble20.txt" using ($1):($4) title 'slackByDag' with points pt 1 lc rgb 'red',\
"weightByDag/ensemble20.txt" using ($1):($4) title 'weightByDag' with points pt 1 lc rgb 'grey'
set output 'plots/others.eps'

set output 'plots/unfairnessToMoney.eps'
set xlabel 'Total Money ($)'
set ylabel 'Unfairness'
plot "commonEntry/ensemble20.txt" using ($1):($7) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble20.txt" using ($1):($7) title 'userPreference' with points pt 2 lc rgb 'green',\
"slackByDag/ensemble20.txt" using ($1):($7) title 'slackByDag' with points pt 1 lc rgb 'red',\
"weightByDag/ensemble20.txt" using ($1):($7) title 'weightByDag' with points pt 1 lc rgb 'grey'
set output 'plots/others.eps'

set output 'plots/unfairnessToTime.eps'
set xlabel 'Execution Time (min)'
set ylabel 'Unfairness'
plot "commonEntry/ensemble20.txt" using ($7):($2/60000) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble20.txt" using ($7):($2/60000) title 'userPreference' with points pt 2 lc rgb 'green',\
"slackByDag/ensemble20.txt" using ($7):($2/60000) title 'slackByDag' with points pt 1 lc rgb 'red',\
"weightByDag/ensemble20.txt" using ($7):($2/60000) title 'weightByDag' with points pt 1 lc rgb 'grey'
set output 'plots/others.eps'


set output 'plots/maxSubdagTime.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time'
plot "commonEntry/ensemble20.txt" using ($1):($6) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble20.txt" using ($1):($6) title 'userPreference' with points pt 2 lc rgb 'green',\
"slackByDag/ensemble20.txt" using ($1):($6) title 'slackByDag' with points pt 1 lc rgb 'red',\
"weightByDag/ensemble20.txt" using ($1):($6) title 'weightByDag' with points pt 1 lc rgb 'grey'
set output 'plots/others.eps'


set output 'plots/subdagSkylineMeanTC.eps'
set xlabel 'Total Money ($)'
set ylabel 'Execution Time'
plot "commonEntry/ensemble20.txt" using ($3):($4) title 'commonEntry' with points pt 8 lc rgb 'blue',\
"userPref/ensemble20.txt" using ($3):($4) title 'userPreference' with points pt 2 lc rgb 'green',\
"slackByDag/ensemble20.txt" using ($3):($4) title 'slackByDag' with points pt 1 lc rgb 'red',\
"weightByDag/ensemble20.txt" using ($3):($4) title 'weightByDag' with points pt 1 lc rgb 'grey'
set output 'plots/others.eps'

#set output 'plots/single.eps'
#set xlabel 'Total Money ($)'
#set ylabel 'Execution Time (s)'
#plot "commonEntry/single1.txt" using ($1):($3) title 'commonEntry' with points pt 8 lc rgb 'blue',\
#"userPref/single1.txt" using ($1):($3) title 'userPreference' with points pt 8 lc rgb 'green'
#set output 'plots/others.eps'
