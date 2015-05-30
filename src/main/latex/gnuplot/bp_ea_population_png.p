set terminal pngcairo size 800,600 dashed enhanced font 'Verdana,10'
set output "bp_ea_population.png"
set key off
set xrange [0:600]
set yrange [0.02:0.03]
set ylabel "MSE" 
set xlabel "population"
set style fill solid
plot "./data/bp_ea_population.dat" using 1:2 with lines