set terminal latex
#set terminal pngcairo size 800,600 dashed enhanced font 'Verdana,10'
set output "bp_ea_cp.tex"
#set output "bp_ea_cp.png"
set key off
set xrange [0.4:1.0]
set yrange [0.02:0.03]
set ylabel "\\rotatebox{90}{MSE}" 
set xlabel "cp"
set style fill solid
plot "./data/bp_ea_cp.dat" using 1:2 with lines