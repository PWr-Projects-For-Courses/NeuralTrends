set terminal latex
#set terminal pngcairo size 800,600 dashed enhanced font 'Verdana,10'
set output "bp_pso_epochs.tex"
#set output "bp_pso_epochs.png"
set key off
set xrange [850:3150]
set yrange [0.02:0.03]
set ylabel "\\rotatebox{90}{MSE}" 
set style fill solid
plot "./data/bp_pso_epochs.dat" using 1:2 with lines