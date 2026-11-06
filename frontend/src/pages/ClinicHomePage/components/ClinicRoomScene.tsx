export default function ClinicRoomScene() {
  return (
    <main className="absolute inset-0 overflow-hidden bg-[#6b765f] text-[#31483f]">
      <img
        src="/clinic-room-first-person.png"
        alt=""
        draggable={false}
        className="absolute inset-0 h-full w-full select-none object-cover object-center"
      />
      <div aria-hidden="true" className="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(29,52,44,0.08),transparent_35%,rgba(73,43,26,0.08))]" />
    </main>
  )
}
