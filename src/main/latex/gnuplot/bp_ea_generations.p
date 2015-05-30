set terminal latex
#set terminal pngcairo size 800,600 dashed enhanced font 'Verdana,10'
set output "bp_ea_generations.tex"
#set output "bp_ea_generations.png"
set key off
set xrange [0:600]
set yrange [0.02:0.03]
set ylabel "\\rotatebox{90}{MSE}" 
set xlabel "generations"
set style fill solid
plot "./data/bp_ea_generations.dat" using 1:2 with lines