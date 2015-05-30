set terminal latex
set output "ea_cp.tex"
set key off
set xrange [0.4:1.0]
set yrange [0.3:0.33]
set ylabel "\\rotatebox{90}{MSE}" 
set xlabel "cp"
set style fill solid
plot "./data/ea_cp.dat" using 1:2 with lines