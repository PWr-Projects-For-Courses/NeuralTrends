set terminal latex
#set terminal pngcairo size 800,600 dashed enhanced font 'Verdana,10'
set output "bp_epochs.tex"
#set output "bp_epochs.png"
set key off
set xrange [850:3150]
set yrange [0:0.03]
set style fill solid
plot "./data/bp_epochs.dat" using 1:2 with lines,\
    "./data/bp_epochs.dat" using 1:($2+0.003):2 with labels