const fs = require('node:fs')

fetch('https://overpass-api.de/api/interpreter', {
    method: 'POST',
    body: "data=" + encodeURIComponent(`
[out:json];

(
    relation[route=tram](around:10000,48.2074,16.3719);
);

out body;
>;
/*added by auto repair*/
(._;>;);
/*end of auto repair*/
out skel qt;
    `)
})
    .then(data => data.json())
    .then(response => {
        const elements = response.elements
        const lines = elements.filter(e => e.type === 'relation')

        const nodes = {}
        elements.filter(element => element.type === 'node')
            .forEach(element => {
                nodes[element.id] = {lat: element.lat, lon: element.lon}
            })

        const head = `
package de.grobox.transportr.map;
import com.mapbox.mapboxsdk.geometry.LatLng;

interface Shibe {
	val coordinates: List<LatLng>
	val color: Int
}

object LineLocationData {
`
        console.log(head)

        const results = lines.map(line => {
            const wayRefs = new Set(line.members
                .filter(member => member.type === 'way' && member.role === '')
                .map(member => member.ref))

            const ways = elements
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

            const waysLinked = Array(ways.length + 1)
            let currentWay = ways[0]
            while (currentWay) {
                try {
                    waysLinked.push(currentWay)
                } catch (err) {
                    // console.log(waysLinked.length)
                    return undefined
                }
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
                .map(node => `\t\tLatLng(${node.lat}, ${node.lon})`)

            const reducedPoints = []
            const reduceFactor = 5;
            for (let i = 0; i < points.length; i = i + reduceFactor) {
                reducedPoints.push(points[i])
            }

            const tags = line.tags
            const name = `_${tags.ref.toLowerCase()}_${tags.from}_${tags.to}`.replaceAll(' ', '').replaceAll(',', '').replaceAll('.', '').replaceAll('-', '_').replaceAll('/', '_')
            const color = 'colour' in tags ? tags.colour : "#ff0000"

            const content = `\tval ${name} = listOf(\n${reducedPoints.join(',\n')}\n\t)`
            console.log(content)

            return {
                name: name,
                color: `0xff${color.replace('#', '').toLowerCase()}.toInt()`
            }
        })

        const footer = `}
        
val lines: List<Shibe> = listOf(\n${results.filter(it => !!it).map(it => 
`\tobject: Shibe {
\t\toverride val coordinates = LineLocationData.${it.name}
\t\toverride val color = ${it.color}
\t}`).join(",\n")}
)`
        console.log(footer)
    });
