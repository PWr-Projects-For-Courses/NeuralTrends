set terminal latex
#set terminal pngcairo size 800,600 dashed enhanced font 'Verdana,10'
set output "pso_generations.tex"
#set output "pso_generations.png"
set key off
set xrange [0:600]
set yrange [0.3:0.33]
set ylabel "\\rotatebox{90}{MSE}" 
set xlabel "generations"
set style fill solid
plot "./data/pso_generations.dat" using 1:2 with lines