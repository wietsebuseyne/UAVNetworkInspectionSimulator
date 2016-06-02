f = open('Germany.geojson', 'r')

scaling = 50

def stringToXCoordinate(x):
    return (float(x)-5.8)*scaling

def stringToYCoordinate(x):
    return (float(x)-47)*scaling

index = 0
nodesIndices = {}
nodes = []

outEdges = '],"edges":['

x2 = 0
y2 = 0
for i, line in enumerate(f):
    split = line.split(',')
##    if x2 != 0:
##        x1 = stringToXCoordinate(split[0])
##        y1 = stringToYCoordinate(split[1])
##        p1 = '{"location":{"x":' + str(x1) + ',"y":' + str(y1) + '}}'
##        p2 = '{"location":{"x":' + str(x2) + ',"y":' + str(y2) + '}}'
##        if not p1 in nodesIndices:
##            nodesIndices[p1] = index
##            nodes.append(p1)
##            index += 1
##        outEdges += '{"source":'+str(nodesIndices[p1])+',"target":'+str(nodesIndices[p2])+'},'

    for i in range(0, len(split)-4, 2):
        x1 = stringToXCoordinate(split[i])
        y1 = stringToYCoordinate(split[i+1])
        x2 = stringToXCoordinate(split[i+2])
        y2 = stringToYCoordinate(split[i+3])
        p1 = '{"location":{"x":' + str(x1) + ',"y":' + str(y1) + '}}'
        p2 = '{"location":{"x":' + str(x2) + ',"y":' + str(y2) + '}}'
        if not p1 in nodesIndices:
            nodesIndices[p1] = index
            nodes.append(p1)
            index += 1
        if not p2 in nodesIndices:
            nodesIndices[p2] = index
            nodes.append(p2)
            index += 1
        outEdges += '{"source":'+str(nodesIndices[p1])+',"target":'+str(nodesIndices[p2])+'},'

outNodes = '{"nodes":['
for n in nodes:
    outNodes += n + ','

outf = open('Germany.json', 'w')
outf.write(outNodes[:-1] + outEdges[:-1] + ']}')
f.close()
outf.close()
