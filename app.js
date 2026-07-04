// Earning Hub Local-First State Engine

// Initial configuration
const INITIAL_POINTS = 1250;
const INITIAL_TRANSACTIONS = [
    { title: "Welcome Register Bonus", type: "BONUS", points: 500, date: "2026-07-01 10:15" },
    { title: "Profile Initial Setup", type: "PROFILE", points: 50, date: "2026-07-02 14:20" },
    { title: "Daily Check-in Reward", type: "DAILY", points: 100, date: "2026-07-03 08:30" },
    { title: "Video Campaign Reward", type: "VIDEO", points: 20, date: "2026-07-03 18:45" },
    { title: "Gaming Arcade Blitz Win", type: "GAMING", points: 150, date: "2026-07-03 21:05" }
];

// App State
let appState = {
    points: INITIAL_POINTS,
    transactions: INITIAL_TRANSACTIONS,
    dailyClaimedTimestamp: 0,
    hasClaimedReferral: false,
    arcadeHighscore: 0
};

// Load state from localStorage
function loadState() {
    const saved = localStorage.getItem("earning_hub_state");
    if (saved) {
        try {
            appState = JSON.parse(saved);
        } catch (e) {
            console.error("Failed to parse saved state", e);
        }
    } else {
        saveState();
    }
    updateUI();
}

// Save state to localStorage
function saveState() {
    localStorage.setItem("earning_hub_state", JSON.stringify(appState));
}

// Update DOM elements
function updateUI() {
    // Balance
    document.getElementById("balance-value").innerText = appState.points.toLocaleString();
    
    // History Rows
    const rowsContainer = document.getElementById("history-rows");
    rowsContainer.innerHTML = "";
    
    // Sort transactions by date descending (newest first)
    const sortedTx = [...appState.transactions].reverse();
    
    sortedTx.forEach(tx => {
        const isEarn = tx.points > 0;
        const colorClass = isEarn ? "text-emerald-400 font-bold" : "text-red-400 font-bold";
        const prefix = isEarn ? "+" : "";
        const badgeColor = getBadgeColor(tx.type);
        
        const row = document.createElement("tr");
        row.className = "border-b border-gray-800/40 hover:bg-gray-900/10 transition-colors";
        row.innerHTML = `
            <td class="py-4 px-6 font-semibold text-white">
                ${tx.title}
                <span class="block text-[10px] text-gray-500 font-normal mt-0.5">${tx.date || getCurrentDateTime()}</span>
            </td>
            <td class="py-4 px-6 text-center">
                <span class="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-md ${badgeColor}">
                    ${tx.type}
                </span>
            </td>
            <td class="py-4 px-6 text-right ${colorClass}">
                ${prefix}${tx.points}
            </td>
        `;
        rowsContainer.appendChild(row);
    });

    // Profile updates
    document.getElementById("profile-completed-quests").innerText = appState.transactions.filter(t => t.points > 0).length;
    document.getElementById("profile-invites-count").innerText = appState.hasClaimedReferral ? "1" : "0";
}

function getBadgeColor(type) {
    switch (type) {
        case "BONUS": return "bg-indigo-500/15 text-indigo-400";
        case "PROFILE": return "bg-blue-500/15 text-blue-400";
        case "DAILY": return "bg-emerald-500/15 text-emerald-400";
        case "VIDEO": return "bg-red-500/15 text-red-400";
        case "GAMING": return "bg-amber-500/15 text-amber-400";
        case "REDEEM": return "bg-rose-500/15 text-rose-400";
        default: return "bg-gray-500/15 text-gray-400";
    }
}

// Helpers
function getCurrentDateTime() {
    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const dd = String(now.getDate()).padStart(2, '0');
    const hh = String(now.getHours()).padStart(2, '0');
    const min = String(now.getMinutes()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
}

// Alert notifications
function showAlert(message, isSuccess = true) {
    const banner = document.getElementById("ui-alert");
    const text = document.getElementById("ui-alert-text");
    
    text.innerText = message;
    banner.classList.remove("hidden", "bg-emerald-500/10", "border-emerald-500/30", "text-emerald-400", "bg-red-500/10", "border-red-500/30", "text-red-400");
    
    if (isSuccess) {
        banner.classList.add("bg-emerald-500/10", "border-emerald-500/30", "text-emerald-400");
    } else {
        banner.classList.add("bg-red-500/10", "border-red-500/30", "text-red-400");
    }
    
    // Auto scroll to top to see alert
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function dismissAlert() {
    document.getElementById("ui-alert").classList.add("hidden");
}

// Tab switcher
function switchTab(tabId) {
    // Hide all tab containers
    document.querySelectorAll(".tab-content").forEach(el => el.classList.add("hidden"));
    // Show chosen tab
    document.getElementById(`tab-${tabId}`).classList.remove("hidden");
    
    // Update sidebar navigation buttons styles
    const tabs = ["earn", "arcade", "redeem", "invite", "history", "profile"];
    tabs.forEach(t => {
        const btn = document.getElementById(`nav-${t}`);
        const mobBtn = document.getElementById(`mob-nav-${t}`);
        
        if (t === tabId) {
            btn.className = "w-full flex items-center gap-3 px-4 py-3 rounded-xl font-semibold transition-all duration-200 bg-indigo-600/15 text-indigo-400";
            if (mobBtn) mobBtn.className = "flex flex-col items-center justify-center text-indigo-400 transition-colors";
        } else {
            btn.className = "w-full flex items-center gap-3 px-4 py-3 rounded-xl font-semibold transition-all duration-200 text-gray-400 hover:bg-gray-800/40 hover:text-white";
            if (mobBtn) mobBtn.className = "flex flex-col items-center justify-center text-gray-400 transition-colors";
        }
    });

    // Special init for Scratch card if shown
    if (tabId === 'earn') {
        initScratchCard();
    }
}

// Feature: Daily Check-in
function claimDailyReward() {
    const cooldown = 24 * 60 * 60 * 1000; // 24 hours
    const now = Date.now();
    
    if (now - appState.dailyClaimedTimestamp < cooldown) {
        const remainingMs = cooldown - (now - appState.dailyClaimedTimestamp);
        const hours = Math.floor(remainingMs / (1000 * 60 * 60));
        const mins = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60));
        showAlert(`You already claimed today! Check back in ${hours}h ${mins}m.`, false);
        return;
    }
    
    appState.points += 100;
    appState.dailyClaimedTimestamp = now;
    appState.transactions.push({
        title: "Daily Check-in Reward",
        type: "DAILY",
        points: 100,
        date: getCurrentDateTime()
    });
    
    saveState();
    updateUI();
    showAlert("Success! Claimed today's check-in bonus. +100 Coins added!");
}

// Feature: Lucky Spin Wheel
let isSpinning = false;
function triggerSpinWheel() {
    if (isSpinning) return;
    
    isSpinning = true;
    const arrow = document.getElementById("wheel-arrow");
    
    // Choose random prize bracket
    const options = [
        { angle: 1440 + 45, prize: 10 },
        { angle: 1440 + 135, prize: 50 },
        { angle: 1440 + 225, prize: 100 },
        { angle: 1440 + 315, prize: 150 }
    ];
    
    const choice = options[Math.floor(Math.random() * options.length)];
    
    arrow.style.transform = `rotate(${choice.angle}deg)`;
    
    setTimeout(() => {
        appState.points += choice.prize;
        appState.transactions.push({
            title: "Lucky Spin Reward",
            type: "GAMING",
            points: choice.prize,
            date: getCurrentDateTime()
        });
        
        saveState();
        updateUI();
        showAlert(`Congrats! Spin wheel stopped. You won +${choice.prize} Coins!`);
        
        // Reset wheel style for future spin
        setTimeout(() => {
            arrow.style.transition = 'none';
            arrow.style.transform = 'rotate(0deg)';
            setTimeout(() => {
                arrow.style.transition = 'transform 4s cubic-bezier(0.1, 0.8, 0.3, 1)';
                isSpinning = false;
            }, 50);
        }, 1000);
    }, 4100);
}

// Feature: Scratch Card Canvas Drawing
let scratchCardClaimed = false;
function initScratchCard() {
    const canvas = document.getElementById("scratch-canvas");
    if (!canvas) return;
    
    const ctx = canvas.getContext("2d");
    const container = canvas.parentElement;
    
    // Match card dimension dynamically
    canvas.width = container.offsetWidth;
    canvas.height = container.offsetHeight;
    
    // Reset state
    scratchCardClaimed = false;
    document.getElementById("scratch-secret-text").innerText = "REVEALED: +50 Coins!";
    
    // Draw silver cover
    ctx.fillStyle = "#4a5568";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // Draw text placeholder on silver
    ctx.fillStyle = "#ffffff";
    ctx.font = "bold 12px sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("SCRATCH HERE", canvas.width / 2, canvas.height / 2);
    
    let isDrawing = false;
    
    function scratch(e) {
        if (!isDrawing || scratchCardClaimed) return;
        
        const rect = canvas.getBoundingClientRect();
        // Handle desktop mouse & mobile touch coords
        const x = (e.clientX || e.touches[0].clientX) - rect.left;
        const y = (e.clientY || e.touches[0].clientY) - rect.top;
        
        ctx.globalCompositeOperation = "destination-out";
        ctx.beginPath();
        ctx.arc(x, y, 16, 0, Math.PI * 2);
        ctx.fill();
        
        checkScratchProgress();
    }
    
    canvas.addEventListener("mousedown", () => isDrawing = true);
    canvas.addEventListener("touchstart", () => isDrawing = true);
    window.addEventListener("mouseup", () => isDrawing = false);
    window.addEventListener("touchend", () => isDrawing = false);
    
    canvas.addEventListener("mousemove", scratch);
    canvas.addEventListener("touchmove", scratch);
}

function checkScratchProgress() {
    if (scratchCardClaimed) return;
    
    const canvas = document.getElementById("scratch-canvas");
    const ctx = canvas.getContext("2d");
    const imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const pixels = imgData.data;
    
    let clearedCount = 0;
    for (let i = 3; i < pixels.length; i += 4) {
        if (pixels[i] === 0) clearedCount++;
    }
    
    const clearedPercent = (clearedCount / (pixels.length / 4)) * 100;
    
    // Reveal fully when >40% scratched
    if (clearedPercent > 40) {
        scratchCardClaimed = true;
        ctx.clearRect(0, 0, canvas.width, canvas.height); // clear entire card
        
        // Award prize
        appState.points += 50;
        appState.transactions.push({
            title: "Scratch Card Secret Prize",
            type: "DAILY",
            points: 50,
            date: getCurrentDateTime()
        });
        
        saveState();
        updateUI();
        showAlert("Awesome! You scratched & revealed a +50 Coins secret reward!");
    }
}

// Feature: Task Offer Wall
function simulateTask(title, coins) {
    showAlert(`Starting verification tracker for "${title}". Points will credit instantly upon success...`);
    setTimeout(() => {
        appState.points += coins;
        appState.transactions.push({
            title: `${title} Completed`,
            type: "VIDEO",
            points: coins,
            date: getCurrentDateTime()
        });
        saveState();
        updateUI();
        showAlert(`Task Verified! +${coins} Coins successfully added to your wallet balance.`, true);
    }, 2000);
}

// Feature: Coin Tapper Arcade Blitz Game Loop
let gameInterval = null;
let gameTimerLeft = 15;
let gameHits = 0;
let isGameRunning = false;

function startArcadeGame() {
    if (isGameRunning) return;
    
    isGameRunning = true;
    gameTimerLeft = 15;
    gameHits = 0;
    
    // Toggle view components
    document.getElementById("game-promo-controls").classList.add("hidden");
    document.getElementById("game-active-panel").classList.remove("hidden");
    
    document.getElementById("game-score-val").innerText = "0";
    document.getElementById("game-timer-val").innerText = "15";
    document.getElementById("game-progress").style.width = "100%";
    
    teleportTargetCoin();
    
    gameInterval = setInterval(() => {
        gameTimerLeft--;
        document.getElementById("game-timer-val").innerText = gameTimerLeft;
        document.getElementById("game-progress").style.width = `${(gameTimerLeft / 15) * 100}%`;
        
        if (gameTimerLeft % 2 === 0) {
            teleportTargetCoin();
        }
        
        if (gameTimerLeft <= 0) {
            endArcadeGame();
        }
    }, 1000);
}

function hitArcadeTarget() {
    if (!isGameRunning) return;
    gameHits++;
    document.getElementById("game-score-val").innerText = gameHits;
    teleportTargetCoin();
    
    // Tap visual effect haptic feedback
    const btn = document.getElementById("game-target-coin");
    btn.classList.add("scale-125");
    setTimeout(() => btn.classList.remove("scale-125"), 100);
}

function teleportTargetCoin() {
    const arena = document.getElementById("game-arena");
    const coin = document.getElementById("game-target-coin");
    
    const maxW = arena.clientWidth - coin.clientWidth - 16;
    const maxH = arena.clientHeight - coin.clientHeight - 16;
    
    const randX = Math.max(8, Math.floor(Math.random() * maxW));
    const randY = Math.max(8, Math.floor(Math.random() * maxH));
    
    coin.style.left = `${randX}px`;
    coin.style.top = `${randY}px`;
}

function endArcadeGame() {
    clearInterval(gameInterval);
    isGameRunning = false;
    
    // Hide panel
    document.getElementById("game-promo-controls").classList.remove("hidden");
    document.getElementById("game-active-panel").classList.add("hidden");
    
    if (gameHits > 0) {
        const reward = gameHits * 10;
        appState.points += reward;
        appState.transactions.push({
            title: "Arcade Arena Blitz Hit-Quest",
            type: "GAMING",
            points: reward,
            date: getCurrentDateTime()
        });
        saveState();
        updateUI();
        showAlert(`Time up! You hit ${gameHits} target coins and earned +${reward} Coins!`, true);
    } else {
        showAlert("Game over! No targets hit. Try again to claim rewards!", false);
    }
}

function quitArcadeGame() {
    clearInterval(gameInterval);
    isGameRunning = false;
    document.getElementById("game-promo-controls").classList.remove("hidden");
    document.getElementById("game-active-panel").classList.add("hidden");
}

// Feature: Redeem Modal Vouchers
let activeRedeemItem = "";
let activeRedeemCost = 0;

function openRedeemForm(itemName, cost) {
    activeRedeemItem = itemName;
    activeRedeemCost = cost;
    
    document.getElementById("redeem-target-name").innerText = itemName;
    document.getElementById("redeem-target-cost").innerText = cost.toLocaleString();
    document.getElementById("redeem-modal").classList.remove("hidden");
}

function closeRedeemForm() {
    document.getElementById("redeem-modal").classList.add("hidden");
}

function submitRedeemRequest(e) {
    e.preventDefault();
    
    if (appState.points < activeRedeemCost) {
        showAlert(`Insufficient coins! You need at least ${activeRedeemCost.toLocaleString()} Coins to redeem this gift voucher.`, false);
        closeRedeemForm();
        return;
    }
    
    const email = document.getElementById("redeem-email").value;
    
    appState.points -= activeRedeemCost;
    appState.transactions.push({
        title: `Redeemed ${activeRedeemItem}`,
        type: "REDEEM",
        points: -activeRedeemCost,
        date: getCurrentDateTime()
    });
    
    saveState();
    updateUI();
    closeRedeemForm();
    showAlert(`Redemption successful! Vouchers request for ${activeRedeemItem} submitted. Voucher code will arrive at "${email}" shortly!`, true);
}

// Feature: Invite and Code Apply
function copyReferralCode() {
    navigator.clipboard.writeText("HUB777");
    showAlert("Referral invite code 'HUB777' copied to clipboard!");
}

function applyReferralCode(e) {
    e.preventDefault();
    const code = document.getElementById("ref-input-code").value.trim().toUpperCase();
    
    if (appState.hasClaimedReferral) {
        showAlert("You have already claimed a signup bonus code!", false);
        return;
    }
    
    if (code === "HUB777") {
        showAlert("You cannot claim your own personal referral code!", false);
        return;
    }
    
    appState.points += 250;
    appState.hasClaimedReferral = true;
    appState.transactions.push({
        title: `Referral Invited Bonus (${code})`,
        type: "BONUS",
        points: 250,
        date: getCurrentDateTime()
    });
    
    saveState();
    updateUI();
    showAlert(`Successfully verified code "${code}"! +250 Signup Coins claimed!`);
}

// App Entry Point
window.onload = function() {
    loadState();
    lucide.createIcons();
    // Pre-cache Scratch card bounds
    setTimeout(initScratchCard, 200);
};
