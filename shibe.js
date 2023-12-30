// response from https://overpass-turbo.eu/
// // @name Cycle Network
//
// /*
// This shows the cycleway and cycleroute network.
// */
//
// [out:json];
//
// (
//     // get cycle route relations
//     relation[route=subway]({{bbox}});
// // get cycleways
// //way[highway=cycleway]({{bbox}});
// //way[highway=path][bicycle=designated]({{bbox}});
// );
//
// out body;
// >;
// /*added by auto repair*/
// (._;>;);
// /*end of auto repair*/
// out skel qt;

const response = require('./shibe.json')

const nodeRefs = response.tram.members
    .filter(member => member.type === 'node' && member.role === 'stop')
    .map(member => member.ref)

const nodes = {}
response.elements
    .filter(element => element.type === 'node')
    .forEach(element => {
        nodes[element.id] ={lat: element.lat, lon: element.lon}
    })

// let res = ''
// nodes.forEach(node => {
//     res += `new LatLng(${node.lat}, ${node.lon}),\n`
// })
// console.log(res)

const wayRefs = new Set(response.tram.members
    .filter(member => member.type === 'way' && member.role === '')
    .map(member => member.ref))

const ways = response.elements
    .filter(e => e.type === 'way')
    .filter(e => wayRefs.has(e.id))
    .map(e => ({
        id: e.id,
        nodes: e.nodes,
        first: e.nodes[0],
        last: e.nodes[e.nodes.length - 1],
    }))

const waysByFirst = new Map();
ways.forEach(way => {
    waysByFirst.set(way.first, way)
})

const waysByLast = new Map();
ways.forEach(way => {
    waysByLast.set(way.last, way)
})

const waysLinked = []

let currentWay = ways[0]
while (currentWay) {
    waysLinked.push(currentWay)
    const nextWay = waysByFirst.get(currentWay.last)
    if (nextWay) {
        currentWay = nextWay
    } else {
        break
    }
}

const points = waysLinked
    .map(way => way.nodes)
    .map(it => {
        it.pop()
        return it
    })
    .flat()
    .map(nodeId => nodes[nodeId])
    .map(node => `new LatLng(${node.lat}, ${node.lon})`)
    .join(',\n')

console.log(points)
