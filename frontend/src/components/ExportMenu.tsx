import { useState } from "react";

type Cell = string | number | null | undefined;

type Props = {
  filename: string;
  columns: string[];
  rows: Cell[][];
};

const escapeHtml = (value: Cell) => String(value ?? "")
  .replace(/&/g, "&amp;")
  .replace(/</g, "&lt;")
  .replace(/>/g, "&gt;")
  .replace(/"/g, "&quot;")
  .replace(/'/g, "&#039;");

function ExportMenu({ filename, columns, rows }: Props) {
  const [open, setOpen] = useState(false);

  const table = `
    <table>
      <thead><tr>${columns.map((column) => `<th>${escapeHtml(column)}</th>`).join("")}</tr></thead>
      <tbody>${rows.map((row) => `<tr>${row.map((cell) => `<td>${escapeHtml(cell)}</td>`).join("")}</tr>`).join("")}</tbody>
    </table>`;

  const download = (content: string, type: string, extension: string) => {
    const blob = new Blob([content], { type });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${filename}.${extension}`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    setOpen(false);
  };

  const exportHtml = () => download(
    `<!doctype html><html lang="fr"><head><meta charset="UTF-8"><title>${escapeHtml(filename)}</title><style>body{font-family:Arial,sans-serif;padding:24px}table{border-collapse:collapse;width:100%}th,td{border:1px solid #cbd5e1;padding:8px;text-align:left}th{background:#e2e8f0}</style></head><body>${table}</body></html>`,
    "text/html;charset=utf-8",
    "html"
  );

  const exportExcel = () => download(
    `<!doctype html><html><head><meta charset="UTF-8"></head><body>${table}</body></html>`,
    "application/vnd.ms-excel;charset=utf-8",
    "xls"
  );

  return (
    <div className="export-menu">
      <button type="button" className="export-menu-trigger" onClick={() => setOpen((value) => !value)}>
        Exporter
        <span aria-hidden="true">›</span>
      </button>
      {open && (
        <div className="export-menu-options">
          <button type="button" onClick={exportHtml}>Exporter en HTML</button>
          <button type="button" onClick={exportExcel}>Exporter en Excel</button>
        </div>
      )}
    </div>
  );
}

export default ExportMenu;
