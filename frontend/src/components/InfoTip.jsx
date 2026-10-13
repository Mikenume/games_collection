// Icono (i) que muestra un aviso: con el ratón se abre al pasar por encima;
// en el móvil se abre al tocar el icono y se cierra al tocar el propio aviso.

import { useId, useState } from 'react';

export default function InfoTip({ label, children }) {
  const [open, setOpen] = useState(false);
  const tipId = useId();

  // Solo el ratón abre/cierra por hover; un toque también dispara pointerenter
  const onHover = (value) => (e) => {
    if (e.pointerType === 'mouse') setOpen(value);
  };

  return (
    <span
      className="info-tip"
      onPointerEnter={onHover(true)}
      onPointerLeave={onHover(false)}
    >
      <button
        type="button"
        className="info-tip-button"
        aria-label={label}
        aria-expanded={open}
        aria-describedby={open ? tipId : undefined}
        onClick={() => setOpen(true)}
        onFocus={() => setOpen(true)}
        onBlur={() => setOpen(false)}
        onKeyDown={(e) => e.key === 'Escape' && setOpen(false)}
      >
        <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
          <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14Zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16Z" />
          <path d="m8.93 6.588-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468
            c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246
            -.686.246-.275 0-.375-.193-.304-.533L8.93 6.588ZM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0Z" />
        </svg>
      </button>
      {open && (
        <span id={tipId} role="tooltip" className="info-tip-bubble" onClick={() => setOpen(false)}>
          {children}
        </span>
      )}
    </span>
  );
}
