export default function ClinicRoomScene() {
  return (
    <>
      <main className="absolute inset-0 z-0 overflow-hidden bg-[#183d5e] text-[#123451]">
        <img
          src="/clinic-room-pixel-main.png"
          alt=""
          draggable={false}
          className="pixel-scene absolute inset-0 h-full w-full select-none object-cover object-center"
        />
        <div aria-hidden="true" className="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(8,33,57,0.1),transparent_38%,rgba(7,38,66,0.12))]" />
      </main>

      {/* 与后景使用同一张图，只露出桌面区域，用来遮挡患者下半身。 */}
      <div aria-hidden="true" className="clinic-desk-foreground pointer-events-none absolute inset-0 z-[25] overflow-hidden">
        <img
          src="/clinic-room-pixel-main.png"
          alt=""
          draggable={false}
          className="pixel-scene absolute inset-0 h-full w-full select-none object-cover object-center"
        />
        <div aria-hidden="true" className="absolute inset-0 bg-[linear-gradient(180deg,rgba(8,33,57,0.1),transparent_38%,rgba(7,38,66,0.12))]" />
      </div>
    </>
  )
}
