my($correct, $predictions) = @ARGV;

open(CORRECT, $correct);
open(PRED, $predictions);

my(@labels);
for (my $i = 1; $i <=8; $i++) {
    for (my $j = 1; $j <= 8; $j++) {
	$labels[$i][$j] = 0;
    }
}

my(@c);
while(<CORRECT>) {
    chomp;
    push @c, (split)[0];
}
close(CORRECT);

while(<PRED>) {
    chomp;
    #my($pred) = substr $p, 0, 1;
    my($p) = substr $_, 0, 1;
    my($t) = shift(@c);
#    print "t:$t p:$p\n";
    $labels[$t][$p]++;
}
close(PRED);

my($total_sum) = 0;
my($total_correct) = 0;
for (my $i = 1; $i <= $#labels; $i++) {
    print "$i: @{$labels[$i]} (";
    my($sum) = 0;
    for (my $j; $j <= $#{$labels[$i]}; $j++) {
	$sum += $labels[$i][$j];
    }
    if ($sum == 0) {
    # No sum = no accuracy since no items
    print "$labels[$i][$i] / $sum = N/A)\n";
  }
  else {
    $acc = $labels[$i][$i] / $sum;
    print "$labels[$i][$i] / $sum = $acc)\n";
  }
    $total_sum += $sum;
    $total_correct += $labels[$i][$i];
}
print "$total_correct / $total_sum = " . ($total_correct / $total_sum) . ")\n";
#    for (my $j = 0; $j <= $#@{$labels[$i]}; $j++) {
#	print "$j: "