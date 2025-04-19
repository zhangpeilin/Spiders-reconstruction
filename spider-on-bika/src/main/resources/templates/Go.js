const a = async (e, t, a) => {
    await async function() {
        if (!r)
            try {
                const e = new Go
                    , t = new URL(n("4h2B"),n.b)
                    , i = await fetch(t)
                    , a = await i.arrayBuffer();
                o = await WebAssembly.compile(a),
                    r = await WebAssembly.instantiate(o, e.importObject),
                    e.run(r)
            } catch (e) {
                throw console.error("Failed to initialize WASM:", e),
                    e
            }
    }();
    const c = i();
    if (void 0 === c.genReqSign)
        throw new Error("WASM function not available");
    const u = a || Date.now();
    if (13 !== u.toString().length)
        throw new Error("Timestamp must be a 13-digit number");
    const s = c.genReqSign(e, t, u);
    if (s.error)
        throw new Error(s.error);
    return s.sign
}