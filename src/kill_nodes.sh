kill $(lsof -t -i:55555);

for j in $(seq 57001 57010)
do
  kill $(lsof -t -i:j);
done