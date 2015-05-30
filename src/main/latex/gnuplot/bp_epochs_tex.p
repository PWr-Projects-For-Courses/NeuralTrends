set terminal latex
set output "bp_epochs.tex"
set key off
set xrange [850:3150]
set yrange [0.02:0.03]
set xlabel "epochs"
set ylabel "\\rotatebox{90}{MSE}" 
set style fill solid
plot "./data/bp_epochs.dat" using 1:2 with lines