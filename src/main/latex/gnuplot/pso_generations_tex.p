set terminal latex
set output "pso_generations.tex"
set key off
set xrange [0:600]
set yrange [0.3:0.33]
set ylabel "\\rotatebox{90}{MSE}" 
set xlabel "generations"
set style fill solid
plot "./data/pso_generations.dat" using 1:2 with lines