set terminal latex
set output "ea_population.tex"
set key off
set xrange [0:600]
set yrange [0.3:0.33]
set ylabel "\\rotatebox{90}{MSE}" 
set xlabel "population"
set style fill solid
plot "./data/ea_population.dat" using 1:2 with lines