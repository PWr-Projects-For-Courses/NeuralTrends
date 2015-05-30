set terminal latex
set output "bp_pso_population.tex"
set key off
set xrange [0:600]
set yrange [0.02:0.045]
set ylabel "\\rotatebox{90}{MSE}" 
set xlabel "population"
set style fill solid
plot "./data/bp_pso_population.dat" using 1:2 with lines