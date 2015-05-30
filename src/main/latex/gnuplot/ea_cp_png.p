set terminal pngcairo size 800,600 dashed enhanced font 'Verdana,10'
set output "ea_cp.png"
set key off
set xrange [0.4:1.0]
set yrange [0.3:0.33]
set ylabel "MSE" 
set xlabel "cp"
set style fill solid
plot "./data/ea_cp.dat" using 1:2 with lines