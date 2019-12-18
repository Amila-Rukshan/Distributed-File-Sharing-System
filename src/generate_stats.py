from dateutil import parser
from bisect import bisect_left
from scipy.stats import norm
import matplotlib.pyplot as plt
from dateutil import parser
import pandas as pd

class discrete_cdf:
    def __init__(self, data):
        self._data = data # must be sorted
        self._data_len = float(len(data))

    def __call__(self, point):
        return (len(self._data[:bisect_left(self._data, point)]) / self._data_len)

def plt_cdf(values, l):
    cdf = discrete_cdf(values)
    xvalues = range(0, int(max(values))+1)
    yvalues = [cdf(point) for point in xvalues]
    plt.plot(xvalues, yvalues, color='b')
    plt.title(l)
#     plt.xlabel('Number of threads')
#     plt.ylabel('Execution time (seconds)')
#     plt.legend()
    plt.show()
    df = pd.DataFrame(values)
    print ("+++++++++++++++++++++++++++++++++")
    print (l)
    print(df.describe())
    print ("+++++++++++++++++++++++++++++++++")

node_degree = []
msg_per_node = []

for i in range(57000, 57012):
    file = open('./node_' + str(i) + '.txt', 'r')
    x = file.readline().replace('\n', '')
    msg_per_node.append(int(x.split(" ")[2]))
    file.readline()
    file.readline()
    x = file.readline().replace('\n', '')
    node_degree.append(int(x.split(" ")[2]))

plt_cdf(node_degree, "CDF of node degree")
plt_cdf(msg_per_node, "CDF of messages per node")

f = open('query_log.txt', 'r')

line = f.readline()

hops = []
latency = []

L = []
while line:
    splitted_msg = line.split(" ")
    if splitted_msg[1] == "REQ":
        L.append([splitted_msg[0], splitted_msg[1]])
    if splitted_msg[1] == "RES":
        if L[len(L)-1][1] == "REQ":
            hops.append(int(splitted_msg[5]))
            time1 = parser.parse(L[len(L)-1][0])
            time2 = parser.parse(splitted_msg[0])
            L.append([splitted_msg[0], splitted_msg[1], splitted_msg[5]])
            latency.append((time2 - time1).seconds)
    line = f.readline()

f.close()


# print (L)
# print ("hops : ", hops)
# print ("latency : ", latency)

plt_cdf(hops, "CDF of hops")
plt_cdf(latency, "CDF of latency")




