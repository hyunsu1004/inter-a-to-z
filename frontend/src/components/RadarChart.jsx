// 신규 npm 의존성 없이 순수 SVG로 그리는 레이더 차트.
// data: [{ name: string, score: number(0~100) }]
export default function RadarChart({ data, size = 280 }) {
  if (!data || data.length === 0) return null

  const center = size / 2
  const radius = size / 2 - 48
  const angleStep = (2 * Math.PI) / data.length

  const pointFor = (index, value) => {
    const angle = angleStep * index - Math.PI / 2
    const r = (value / 100) * radius
    return {
      x: center + r * Math.cos(angle),
      y: center + r * Math.sin(angle),
    }
  }

  const dataPoints = data.map((d, i) => pointFor(i, d.score))
  const polygonPoints = dataPoints.map((p) => `${p.x},${p.y}`).join(' ')
  const rings = [25, 50, 75, 100]

  return (
    <svg viewBox={`0 0 ${size} ${size}`} width="100%" style={{ maxWidth: size }}>
      {rings.map((ring) => (
        <polygon
          key={ring}
          points={data.map((_, i) => {
            const p = pointFor(i, ring)
            return `${p.x},${p.y}`
          }).join(' ')}
          fill="none"
          stroke="var(--border)"
          strokeWidth="1"
        />
      ))}

      {data.map((_, i) => {
        const edge = pointFor(i, 100)
        return (
          <line
            key={i}
            x1={center}
            y1={center}
            x2={edge.x}
            y2={edge.y}
            stroke="var(--border)"
            strokeWidth="1"
          />
        )
      })}

      <polygon
        points={polygonPoints}
        fill="var(--brand-300)"
        fillOpacity="0.32"
        stroke="var(--brand-300)"
        strokeWidth="2"
      />

      {dataPoints.map((p, i) => (
        <circle key={i} cx={p.x} cy={p.y} r="3" fill="var(--brand-400)" />
      ))}

      {data.map((d, i) => {
        const labelPoint = pointFor(i, 122)
        const shortName = d.name.length > 6 ? d.name.slice(0, 5) + '…' : d.name
        return (
          <text
            key={i}
            x={labelPoint.x}
            y={labelPoint.y}
            fontSize="10"
            fill="var(--text-secondary)"
            textAnchor="middle"
            dominantBaseline="middle"
          >
            {shortName}
          </text>
        )
      })}
    </svg>
  )
}
