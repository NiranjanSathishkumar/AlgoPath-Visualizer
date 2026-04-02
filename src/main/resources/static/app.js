// ─────────────────────────────────────────────
//  API Base URL — resolves automatically per environment
//  · Local dev  → uses relative paths (same origin, port 8080)
//  · Production → points to Render-hosted backend
// ─────────────────────────────────────────────
const isLocal =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1";

const API_BASE_URL = isLocal
    ? ""
    : "https://algopath-visualizer-api.onrender.com";

// ─────────────────────────────────────────────
//  DOM References
// ─────────────────────────────────────────────
const gridContainer = document.getElementById('maze-grid');
const btnGenerate = document.getElementById('btn-generate');
const btnSolve = document.getElementById('btn-solve');
const btnResults = document.getElementById('btn-results');
const widthInput = document.getElementById('maze-width');
const heightInput = document.getElementById('maze-height');
const complexitySelect = document.getElementById('maze-complexity');
const statusBadge = document.getElementById('race-status');
const emptyState = document.getElementById('empty-state');
const resultsModal = document.getElementById('results-modal');
const btnCloseModal = document.getElementById('btn-close-modal');
const resultsTbody = document.getElementById('results-tbody');
const btnTogglePanel = document.getElementById('btn-toggle-panel');
const sidePanel = document.querySelector('.panel');

const btnEditMaze = document.getElementById('btn-edit-maze');
const navInteractive = document.getElementById('nav-interactive-controls');
const navMain = document.querySelector('.nav-controls:not(.nav-interactive)');
const btnInteractiveSave = document.getElementById('btn-interactive-save');
const btnInteractiveDiscard = document.getElementById('btn-interactive-discard');
const toolsPalette = document.getElementById('tools-palette');

const noSolutionModal = document.getElementById('no-solution-modal');
const btnCloseNoSolution = document.getElementById('btn-close-no-solution');

// Playback
const toolbar = document.getElementById('toolbar');
const labelAlgo = document.getElementById('active-algo-label');
const btnPlay = document.getElementById('btn-play');
const btnPause = document.getElementById('btn-pause');
const btnReset = document.getElementById('btn-reset');
const speedSlider = document.getElementById('speed-slider');

// View / Zoom / Pan
const viewport = document.getElementById('grid-viewport');
const transformLayer = document.getElementById('grid-transform-layer');
const btnZoomIn = document.getElementById('btn-zoom-in');
const btnZoomOut = document.getElementById('btn-zoom-out');
const btnFit = document.getElementById('btn-fit');
const zoomLabel = document.getElementById('zoom-label');

// ─────────────────────────────────────────────
//  Application State
// ─────────────────────────────────────────────
let currentMazeData = null;
let currentResults = {};
let playbackState = 'STOPPED';   // PLAYING | PAUSED | STOPPED
let activeAlgorithm = null;
let currentAnimationStep = 0;
let isAnimatingPath = false;
let activeAnimationTimeout = null;

// Drawing state
let activeTool = 'START';
let isPainting = false;
let customStart = null;
let customEnd = null;
let changedCells = [];
let savedChangedCells = [];
let savedStart = null;
let savedEnd = null;

// Cell pixel size used when rendering (not affected by CSS zoom)
let CELL_PX = 20;

// ─────────────────────────────────────────────
//  Zoom / Pan State
// ─────────────────────────────────────────────
const ZOOM_STEP = 0.15;
const ZOOM_MIN = 0.25;
const ZOOM_MAX = 4.0;
let zoomLevel = 1.0;

// Pan (drag) state
let isPanning = false;
let panStartX = 0;
let panStartY = 0;
let panScrollX = 0;
let panScrollY = 0;

// ─────────────────────────────────────────────
//  Initialisation
// ─────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    generateMaze();

    btnGenerate.addEventListener('click', generateMaze);
    btnSolve.addEventListener('click', solveMaze);
    if (btnResults) btnResults.addEventListener('click', openResultsModal);
    if (btnCloseModal) btnCloseModal.addEventListener('click', closeResultsModal);
    if (resultsModal) {
        resultsModal.addEventListener('click', (e) => {
            if (e.target === resultsModal) closeResultsModal();
        });
    }
    document.querySelectorAll('#results-table th[data-sort]').forEach(th => {
        th.addEventListener('click', () => {
            sortTable(th.getAttribute('data-sort'), th);
        });
    });

    btnPlay.addEventListener('click', resumePlayback);
    btnPause.addEventListener('click', pausePlayback);
    btnReset.addEventListener('click', resetPlayback);
    
    if (btnEditMaze) btnEditMaze.addEventListener('click', enterInteractiveMode);
    if (btnInteractiveSave) btnInteractiveSave.addEventListener('click', saveInteractiveMode);
    if (btnInteractiveDiscard) btnInteractiveDiscard.addEventListener('click', discardInteractiveMode);

    if (btnCloseNoSolution) {
        btnCloseNoSolution.addEventListener('click', () => {
            noSolutionModal.setAttribute('aria-hidden', 'true');
        });
    }
    
    const btnStepPrev = document.getElementById('btn-step-prev');
    const btnStepNext = document.getElementById('btn-step-next');
    if (btnStepPrev) btnStepPrev.addEventListener('click', stepBackward);
    if (btnStepNext) btnStepNext.addEventListener('click', stepForward);

    document.querySelectorAll('.tool-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tool-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            activeTool = btn.getAttribute('data-tool');
        });
    });

    if (btnTogglePanel && sidePanel) {
        btnTogglePanel.addEventListener('click', () => {
            const isOpen = sidePanel.classList.toggle('panel-open');
            btnTogglePanel.classList.toggle('active', isOpen);
        });

        // Close panel when clicking outside on mobile
        document.addEventListener('click', (e) => {
            if (window.innerWidth <= 900 && sidePanel.classList.contains('panel-open') &&
                !sidePanel.contains(e.target) && !btnTogglePanel.contains(e.target)) {
                sidePanel.classList.remove('panel-open');
                btnTogglePanel.classList.remove('active');
            }
        });
    }

    // Zoom buttons
    btnZoomIn.addEventListener('click', () => applyZoom(zoomLevel + ZOOM_STEP));
    btnZoomOut.addEventListener('click', () => applyZoom(zoomLevel - ZOOM_STEP));
    btnFit.addEventListener('click', fitToScreen);

    // Mouse-wheel zoom (Ctrl + scroll = zoom, plain scroll = pan)
    viewport.addEventListener('wheel', onWheel, { passive: false });

    // Pan via click-drag
    viewport.addEventListener('mousedown', onPanStart);
    window.addEventListener('mousemove', onPanMove);
    window.addEventListener('mouseup', onPanEnd);
});

// ─────────────────────────────────────────────
//  1. Generate Maze
// ─────────────────────────────────────────────
async function generateMaze() {
    clearGrid();
    resetDashboard();
    customStart = null;
    customEnd = null;
    changedCells = [];
    setStatus('Generating…', 'waiting');
    hideEmptyState(false);

    const w = parseInt(widthInput.value, 10) || 30;
    const h = parseInt(heightInput.value, 10) || 15;
    const complexity = complexitySelect ? complexitySelect.value : 'MEDIUM';

    try {
        const response = await fetch(`${API_BASE_URL}/maze/generate?width=${w}&height=${h}&complexity=${complexity}`);
        const data = await response.json();

        currentMazeData = data.maze;
        renderStaticGrid(currentMazeData);
        setStatus('Ready', 'waiting');
        hideEmptyState(true);

        // Fit grid nicely on first load
        setTimeout(fitToScreen, 80);
        
        if (btnEditMaze) btnEditMaze.style.display = 'inline-flex';
    } catch (err) {
        console.error('Error generating maze:', err);
        setStatus('Error', 'waiting');
    }
}

// ─────────────────────────────────────────────
//  2. Solve Maze
// ─────────────────────────────────────────────
async function solveMaze() {
    if (!currentMazeData) return;
    resetDashboard();
    setStatus('Racing Solvers…', 'racing');

    const payload = {
        startRow: customStart ? customStart.r : 0,
        startCol: customStart ? customStart.c : 0,
        endRow: customEnd ? customEnd.r : currentMazeData.height - 1,
        endCol: customEnd ? customEnd.c : currentMazeData.width - 1,
        changedCells: changedCells
    };

    try {
        const response = await fetch(`${API_BASE_URL}/maze/solve`, { 
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (response.ok) pollForResults();
    } catch (err) {
        console.error('Error solving maze:', err);
    }
}

// ─────────────────────────────────────────────
//  3. Polling for results
// ─────────────────────────────────────────────
async function pollForResults() {
    let completeCount = 0;

    const pollInterval = setInterval(async () => {
        const res = await fetch(`${API_BASE_URL}/maze/results`);
        const resultsArray = await res.json();

        resultsArray.forEach(result => {
            const algo = result.algorithmName;
            if (!currentResults[algo]) {
                completeCount++;
                currentResults[algo] = result;
                populateCard(algo, result);
            }
        });

        if (completeCount >= 5) {
            clearInterval(pollInterval);
            setStatus('Race Complete!', 'complete');
            computeWinners();
        }
    }, 100);
}

// ─────────────────────────────────────────────
//  Rankings & Checks
// ─────────────────────────────────────────────
function computeWinners() {
    let anyPathFound = false;
    Object.values(currentResults).forEach(res => {
        if (res.pathLength > 0) anyPathFound = true;
    });

    if (!anyPathFound && noSolutionModal) {
        noSolutionModal.setAttribute('aria-hidden', 'false');
    }

    const sorted = Object.values(currentResults).sort((a, b) => {
        if (a.pathCost !== b.pathCost) return a.pathCost - b.pathCost;
        if (a.executionTimeMs !== b.executionTimeMs) return a.executionTimeMs - b.executionTimeMs;
        return a.nodesExplored - b.nodesExplored;
    });
    // Medals removed as requested
    if (btnResults) btnResults.style.display = 'inline-flex';
}

// ─────────────────────────────────────────────
//  Results Modal & Table
// ─────────────────────────────────────────────
let currentSortKey = null;
let currentSortAsc = true;

function openResultsModal() {
    if (!resultsModal) return;
    populateTable();
    resultsModal.setAttribute('aria-hidden', 'false');
}

function closeResultsModal() {
    if (!resultsModal) return;
    resultsModal.setAttribute('aria-hidden', 'true');
}

function populateTable() {
    if (!resultsTbody) return;
    resultsTbody.innerHTML = '';

    let rowsData = Object.values(currentResults).map(r => {
        let name = r.algorithmName.replace('Solver', '');
        if (name === 'GreedyBestFirst') name = 'Greedy';

        let color = '#38bdf8';
        if (name.includes('DFS')) color = '#f472b6';
        if (name.includes('AStar')) color = '#fb923c';
        if (name.includes('Dijkstra')) color = '#a855f7';
        if (name.includes('Greedy')) color = '#14b8a6';

        return {
            name: name,
            time: r.executionTimeMs,
            nodes: r.nodesExplored,
            path: r.pathLength,
            cost: r.pathCost,
            color: color
        };
    });

    if (currentSortKey) {
        rowsData.sort((a, b) => {
            let valA = a[currentSortKey];
            let valB = b[currentSortKey];
            if (typeof valA === 'string') {
                return currentSortAsc ? valA.localeCompare(valB) : valB.localeCompare(valA);
            }
            return currentSortAsc ? valA - valB : valB - valA;
        });
    }

    rowsData.forEach(row => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <div class="algo-name">
                    <span class="col-dot" style="background:${row.color}; box-shadow: 0 0 8px ${row.color}"></span>
                    ${row.name}
                </div>
            </td>
            <td class="num-col">${row.time}</td>
            <td class="num-col">${row.nodes}</td>
            <td class="num-col">${row.path}</td>
            <td class="num-col" style="color:var(--accent-hi)">${row.cost}</td>
        `;
        resultsTbody.appendChild(tr);
    });
}

function sortTable(key, thElement) {
    if (currentSortKey === key) {
        currentSortAsc = !currentSortAsc;
    } else {
        currentSortKey = key;
        currentSortAsc = true;
    }

    document.querySelectorAll('#results-table th').forEach(th => {
        th.classList.remove('sorted-asc', 'sorted-desc');
    });
    thElement.classList.add(currentSortAsc ? 'sorted-asc' : 'sorted-desc');

    populateTable();
}

// ─────────────────────────────────────────────
//  Dashboard helpers
// ─────────────────────────────────────────────
function populateCard(algo, result) {
    const card = document.getElementById(`card-${algo}`);
    if (!card) return;
    card.classList.add('has-data');
    card.querySelector('.time').textContent = `${result.executionTimeMs} ms`;
    card.querySelector('.nodes').textContent = result.nodesExplored;
    card.querySelector('.path').textContent = result.pathLength;
    card.querySelector('.cost').textContent = result.pathCost;
}

function resetDashboard() {
    currentResults = {};
    stopEngineFully();
    toolbar.style.display = 'none';
    if (btnResults) btnResults.style.display = 'none';

    if (currentMazeData) {
        renderStaticGrid(currentMazeData);
        changedCells.forEach(cell => {
            const domCell = document.getElementById(`cell-${cell.row}-${cell.column}`);
            if(domCell) domCell.setAttribute('data-terrain', cell.terrain);
        });
    }

    document.querySelectorAll('.algo-card').forEach(card => {
        card.classList.remove('has-data', 'playing');
        card.querySelector('.time').textContent = '—';
        card.querySelector('.nodes').textContent = '—';
        card.querySelector('.path').textContent = '—';
        card.querySelector('.cost').textContent = '—';
    });
}

function setStatus(text, cls) {
    statusBadge.className = `status-chip ${cls}`;
    statusBadge.innerHTML = '';
    const dotEl = document.createElement('span');
    dotEl.className = 'status-dot';
    const textEl = document.createElement('span');
    textEl.className = 'status-text';
    textEl.textContent = text;
    statusBadge.appendChild(dotEl);
    statusBadge.appendChild(textEl);
}

function hideEmptyState(hidden) {
    emptyState.style.display = hidden ? 'none' : 'flex';
}

// ─────────────────────────────────────────────
//  4. Render Grid
// ─────────────────────────────────────────────
function renderStaticGrid(mazeObj) {
    const w = mazeObj.width;
    const h = mazeObj.height;

    gridContainer.style.gridTemplateColumns = `repeat(${w}, ${CELL_PX}px)`;
    gridContainer.style.gridTemplateRows = `repeat(${h}, ${CELL_PX}px)`;
    gridContainer.innerHTML = '';

    mazeObj.grid.forEach(row => {
        row.forEach(cellObj => {
            const cell = document.createElement('div');
            cell.classList.add('maze-cell');
            cell.id = `cell-${cellObj.row}-${cellObj.column}`;

            cell.style.width = `${CELL_PX}px`;
            cell.style.height = `${CELL_PX}px`;

            if (!cellObj.topWall) cell.style.borderTop = 'none';
            if (!cellObj.rightWall) cell.style.borderRight = 'none';
            if (!cellObj.bottomWall) cell.style.borderBottom = 'none';
            if (!cellObj.leftWall) cell.style.borderLeft = 'none';

            // Apply terrain type for styling
            if (cellObj.terrain) {
                cell.setAttribute('data-terrain', cellObj.terrain);
            }

            let isStart = customStart ? (customStart.r === cellObj.row && customStart.c === cellObj.column) : (cellObj.row === 0 && cellObj.column === 0);
            let isEnd = customEnd ? (customEnd.r === cellObj.row && customEnd.c === cellObj.column) : (cellObj.row === h - 1 && cellObj.column === w - 1);

            if (isStart) {
                cell.classList.add('start');
            } else if (isEnd) {
                cell.classList.add('end');
            }

            cell.addEventListener('mousedown', (e) => {
                if (e.button !== 0) return; // Only left click
                isPainting = true;
                applyToolToCell(cell, cellObj.row, cellObj.column);
            });
            cell.addEventListener('mouseenter', () => {
                if (isPainting) applyToolToCell(cell, cellObj.row, cellObj.column);
            });
            cell.addEventListener('mouseup', () => {
                isPainting = false;
            });

            gridContainer.appendChild(cell);
        });
    });
    
    gridContainer.addEventListener('mouseleave', () => isPainting = false);
    if (!customStart) customStart = { r: 0, c: 0 };
    if (!customEnd) customEnd = { r: h - 1, c: w - 1 };
}

function applyToolToCell(domCell, r, c) {
    if (!activeTool) return;
    if (activeTool === 'START') {
        document.querySelectorAll('.maze-cell.start').forEach(el => el.classList.remove('start'));
        domCell.classList.add('start');
        customStart = { r, c };
        return;
    }
    if (activeTool === 'END') {
        document.querySelectorAll('.maze-cell.end').forEach(el => el.classList.remove('end'));
        domCell.classList.add('end');
        customEnd = { r, c };
        return;
    }
    
    if (customStart && customStart.r === r && customStart.c === c) return;
    if (customEnd && customEnd.r === r && customEnd.c === c) return;
    
    domCell.setAttribute('data-terrain', activeTool);
    changedCells.push({ row: r, column: c, terrain: activeTool });
}

function clearGrid() {
    gridContainer.innerHTML = '';
}

// ─────────────────────────────────────────────
//  Interactive Mode Lifecycle
// ─────────────────────────────────────────────
function enterInteractiveMode() {
    if (navMain) navMain.style.display = 'none';
    if (navInteractive) navInteractive.style.display = 'flex';
    if (sidePanel) sidePanel.style.display = 'none';
    if (toolsPalette) toolsPalette.style.display = 'flex';
    
    savedChangedCells = JSON.parse(JSON.stringify(changedCells));
    savedStart = customStart ? { ...customStart } : null;
    savedEnd = customEnd ? { ...customEnd } : null;
}

function saveInteractiveMode() {
    if (navMain) navMain.style.display = 'flex';
    if (navInteractive) navInteractive.style.display = 'none';
    if (sidePanel) sidePanel.style.display = 'flex';
    if (toolsPalette) toolsPalette.style.display = 'none';
    
    // Automatically trigger race
    setTimeout(solveMaze, 150);
}

function discardInteractiveMode() {
    if (navMain) navMain.style.display = 'flex';
    if (navInteractive) navInteractive.style.display = 'none';
    if (sidePanel) sidePanel.style.display = 'flex';
    if (toolsPalette) toolsPalette.style.display = 'none';
    
    changedCells = savedChangedCells;
    customStart = savedStart;
    customEnd = savedEnd;
    
    clearGrid();
    renderStaticGrid(currentMazeData);
    
    changedCells.forEach(cell => {
        const domCell = document.getElementById(`cell-${cell.row}-${cell.column}`);
        if(domCell) domCell.setAttribute('data-terrain', cell.terrain);
    });
}

// ─────────────────────────────────────────────
//  5. Engine Controls
// ─────────────────────────────────────────────
function stopEngineFully() {
    playbackState = 'STOPPED';
    activeAlgorithm = null;
    currentAnimationStep = 0;
    isAnimatingPath = false;
    if (activeAnimationTimeout) clearTimeout(activeAnimationTimeout);

    document.querySelectorAll('.maze-cell.visited, .maze-cell.path').forEach(el => {
        el.classList.remove('visited', 'path');
    });
}

window.playAlgorithm = function (algo) {
    if (!currentResults[algo] || !currentMazeData) return;

    stopEngineFully();

    // Highlight active card
    document.querySelectorAll('.algo-card').forEach(c => c.classList.remove('playing'));
    const activeCard = document.getElementById(`card-${algo}`);
    if (activeCard) activeCard.classList.add('playing');

    activeAlgorithm = algo;
    const shortName = algo.replace('Solver', '');
    labelAlgo.textContent = `▶  ${shortName}`;
    toolbar.style.display = 'flex';

    resumePlayback();
};

function resumePlayback() {
    if (!activeAlgorithm || playbackState === 'PLAYING') return;
    playbackState = 'PLAYING';
    loopAnimation();
}

function pausePlayback() {
    playbackState = 'PAUSED';
    if (activeAnimationTimeout) clearTimeout(activeAnimationTimeout);
}

function resetPlayback() {
    const retain = activeAlgorithm;
    stopEngineFully();
    activeAlgorithm = retain;
    playbackState = 'STOPPED';
}

function loopAnimation() {
    if (playbackState !== 'PLAYING') return;
    const result = currentResults[activeAlgorithm];
    if (!result) return;
    
    const delay = isAnimatingPath ? Math.max(10, parseInt(speedSlider.value, 10) / 2) : parseInt(speedSlider.value, 10);
    
    let proceeded = applyAnimationFrame(result.visitedOrder, result.finalPath, true);
    if (proceeded) {
        activeAnimationTimeout = setTimeout(loopAnimation, delay);
    }
}

function applyAnimationFrame(visitedNodes, finalPath, forward) {
    const totalFrames = visitedNodes.length + finalPath.length;
    
    if (forward) {
        if (currentAnimationStep >= totalFrames) {
            playbackState = 'STOPPED';
            const shortName2 = activeAlgorithm ? activeAlgorithm.replace('Solver', '') : '';
            labelAlgo.textContent = `${shortName2}  ·  Done`;
            return false; // Reached end
        }

        if (currentAnimationStep < visitedNodes.length) {
            isAnimatingPath = false;
            const node = visitedNodes[currentAnimationStep];
            const domNode = document.getElementById(`cell-${node.row}-${node.column}`);
            if (domNode && !domNode.classList.contains('start') && !domNode.classList.contains('end')) {
                domNode.classList.add('visited');
            }
        } else {
            isAnimatingPath = true;
            const pIdx = currentAnimationStep - visitedNodes.length;
            const node = finalPath[pIdx];
            const domNode = document.getElementById(`cell-${node.row}-${node.column}`);
            if (domNode && !domNode.classList.contains('start') && !domNode.classList.contains('end')) {
                domNode.classList.add('path');
            }
        }
        currentAnimationStep++;
        return true;
    } else {
        // Step backward
        if (currentAnimationStep <= 0) return false;
        
        currentAnimationStep--; 
        
        if (currentAnimationStep < visitedNodes.length) {
            isAnimatingPath = false;
            const node = visitedNodes[currentAnimationStep];
            const domNode = document.getElementById(`cell-${node.row}-${node.column}`);
            if (domNode) domNode.classList.remove('visited');
        } else {
            isAnimatingPath = true;
            const pIdx = currentAnimationStep - visitedNodes.length;
            const node = finalPath[pIdx];
            const domNode = document.getElementById(`cell-${node.row}-${node.column}`);
            if (domNode) domNode.classList.remove('path');
        }
        return true;
    }
}

function stepForward() {
    if (!activeAlgorithm || playbackState === 'PLAYING') return;
    const result = currentResults[activeAlgorithm];
    if (!result) return;
    applyAnimationFrame(result.visitedOrder, result.finalPath, true);
}

function stepBackward() {
    if (!activeAlgorithm || playbackState === 'PLAYING') return;
    const result = currentResults[activeAlgorithm];
    if (!result) return;
    applyAnimationFrame(result.visitedOrder, result.finalPath, false);
}

// ─────────────────────────────────────────────
//  Zoom helpers
// ─────────────────────────────────────────────
function applyZoom(newZoom, pivotX, pivotY) {
    const clamped = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, newZoom));

    // Preserve scroll position relative to pivot so zoom feels centred
    if (pivotX !== undefined && pivotY !== undefined) {
        const ratio = clamped / zoomLevel;
        viewport.scrollLeft = (viewport.scrollLeft + pivotX) * ratio - pivotX;
        viewport.scrollTop = (viewport.scrollTop + pivotY) * ratio - pivotY;
    }

    zoomLevel = clamped;
    transformLayer.style.transform = `scale(${zoomLevel})`;
    zoomLabel.textContent = `${Math.round(zoomLevel * 100)}%`;
}

function fitToScreen() {
    if (!currentMazeData) return;

    const w = currentMazeData.width;
    const h = currentMazeData.height;

    // Grid intrinsic size (cells + padding inside transform-layer)
    const gridW = w * CELL_PX + 112;   // 56px padding each side
    const gridH = h * CELL_PX + 112;

    const vW = viewport.clientWidth;
    const vH = viewport.clientHeight;

    const fit = Math.min(vW / gridW, vH / gridH, ZOOM_MAX);
    applyZoom(fit);
}

// ─────────────────────────────────────────────
//  Mouse-wheel handler
// ─────────────────────────────────────────────
function onWheel(e) {
    if (e.ctrlKey || e.metaKey) {
        e.preventDefault();
        const rect = viewport.getBoundingClientRect();
        const pivotX = e.clientX - rect.left;
        const pivotY = e.clientY - rect.top;
        const delta = e.deltaY < 0 ? ZOOM_STEP : -ZOOM_STEP;
        applyZoom(zoomLevel + delta, pivotX, pivotY);
    }
    // plain scroll is handled natively by overflow:scroll
}

// ─────────────────────────────────────────────
//  Pan / drag
// ─────────────────────────────────────────────
function onPanStart(e) {
    if (e.button !== 0) return;
    isPanning = true;
    panStartX = e.clientX;
    panStartY = e.clientY;
    panScrollX = viewport.scrollLeft;
    panScrollY = viewport.scrollTop;
    viewport.style.cursor = 'grabbing';
}

function onPanMove(e) {
    if (!isPanning) return;
    const dx = e.clientX - panStartX;
    const dy = e.clientY - panStartY;
    viewport.scrollLeft = panScrollX - dx;
    viewport.scrollTop = panScrollY - dy;
}

function onPanEnd() {
    if (!isPanning) return;
    isPanning = false;
    viewport.style.cursor = 'grab';
}

// ─────────────────────────────────────────────
//  Legend hover/tap toggle
// ─────────────────────────────────────────────
(function () {
    const trigger = document.getElementById('legend-trigger');
    const panel = document.getElementById('legend-panel');
    if (!trigger || !panel) return;

    // Toggle on tap (mobile) — hover handled by CSS
    trigger.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = panel.classList.contains('visible');
        panel.classList.toggle('visible', !isOpen);
        panel.setAttribute('aria-hidden', isOpen ? 'true' : 'false');
    });

    // Close when clicking outside
    document.addEventListener('click', (e) => {
        if (!trigger.contains(e.target) && !panel.contains(e.target)) {
            panel.classList.remove('visible');
            panel.setAttribute('aria-hidden', 'true');
        }
    });

    // Keep panel open when mouse moves between trigger and panel
    trigger.addEventListener('mouseenter', () => {
        panel.classList.add('visible');
        panel.setAttribute('aria-hidden', 'false');
    });
    trigger.addEventListener('mouseleave', (e) => {
        if (!panel.matches(':hover')) {
            panel.classList.remove('visible');
            panel.setAttribute('aria-hidden', 'true');
        }
    });
    panel.addEventListener('mouseleave', (e) => {
        if (!trigger.matches(':hover')) {
            panel.classList.remove('visible');
            panel.setAttribute('aria-hidden', 'true');
        }
    });
})();
