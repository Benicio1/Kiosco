// crunchyroll-tv.js - Módulo de Integración y Navegación TV para Crunchyroll
import { playVideo } from './youtube-tv.js';

const ANIMES = [
  {
    id: 'one_piece',
    title: 'One Piece',
    genre: 'Aventura • Piratas • Shonen',
    episodes: '+1100 Episodios',
    dub: 'Sub • Doblado',
    category: 'popular',
    poster: 'https://images.alphacoders.com/134/thumb-1920-1347040.png',
    url: 'https://www.crunchyroll.com/es/series/GRMG8ZQZR/one-piece',
    trailerId: 'MCb13lbZ5vk'
  },
  {
    id: 'jujutsu_kaisen',
    title: 'Jujutsu Kaisen',
    genre: 'Acción • Sobrenatural • Hechicería',
    episodes: '47 Episodios',
    dub: 'Sub • Doblado',
    category: 'popular',
    poster: 'https://images7.alphacoders.com/113/thumb-1920-1132009.jpg',
    url: 'https://www.crunchyroll.com/es/series/GRDV0019R/jujutsu-kaisen',
    trailerId: '4A_XgUi4f-4'
  },
  {
    id: 'demon_slayer',
    title: 'Demon Slayer: Kimetsu no Yaiba',
    genre: 'Espadachines • Demonios • Acción',
    episodes: '55 Episodios',
    dub: 'Sub • Doblado',
    category: 'popular',
    poster: 'https://images8.alphacoders.com/100/thumb-1920-1004568.jpg',
    url: 'https://www.crunchyroll.com/es/series/GY5P48XEY/demon-slayer-kimetsu-no-yaiba',
    trailerId: 'VQGCKyvzIM4'
  },
  {
    id: 'solo_leveling',
    title: 'Solo Leveling (Arise)',
    genre: 'Fantasía Oscura • Mazmorras • Acción',
    episodes: '12 Episodios',
    dub: 'Sub • Doblado',
    category: 'temporada',
    poster: 'https://images.alphacoders.com/134/thumb-1920-1345638.png',
    url: 'https://www.crunchyroll.com/es/series/GEXH3W25M/solo-leveling',
    trailerId: '91_t7OspKjQ'
  },
  {
    id: 'chainsaw_man',
    title: 'Chainsaw Man',
    genre: 'Gore • Demonios • Comedia Negra',
    episodes: '12 Episodios',
    dub: 'Sub • Doblado',
    category: 'popular',
    poster: 'https://images3.alphacoders.com/129/thumb-1920-1293370.jpg',
    url: 'https://www.crunchyroll.com/es/series/GVDHX8QNW/chainsaw-man',
    trailerId: 'jk7Q4nnpZJw'
  },
  {
    id: 'attack_on_titan',
    title: 'Attack on Titan (Shingeki no Kyojin)',
    genre: 'Titanes • Misterio • Drama Militar',
    episodes: '89 Episodios',
    dub: 'Completo',
    category: 'shonen',
    poster: 'https://images6.alphacoders.com/133/thumb-1920-1331707.jpeg',
    url: 'https://www.crunchyroll.com/es/series/GR751KNZY/attack-on-titan',
    trailerId: 'M_OauHnAFc8'
  },
  {
    id: 'dragon_ball_super',
    title: 'Dragon Ball Super',
    genre: 'Artes Marciales • Dioses • Peleas',
    episodes: '131 Episodios',
    dub: 'Sub • Doblado',
    category: 'shonen',
    poster: 'https://images4.alphacoders.com/712/thumb-1920-712399.jpg',
    url: 'https://www.crunchyroll.com/es/series/GR19V7816/dragon-ball-super',
    trailerId: '3L-0k6pD4pI'
  },
  {
    id: 'naruto_shippuden',
    title: 'Naruto Shippuden',
    genre: 'Ninjas • Amistad • Batallas',
    episodes: '500 Episodios',
    dub: 'Sub • Doblado',
    category: 'shonen',
    poster: 'https://images3.alphacoders.com/131/thumb-1920-1311090.jpeg',
    url: 'https://www.crunchyroll.com/es/series/GY24P7V4R/naruto-shippuden',
    trailerId: 'QczGo-nCGkc'
  },
  {
    id: 'spy_family',
    title: 'SPY x FAMILY',
    genre: 'Comedia • Espías • Recuentos de la vida',
    episodes: '37 Episodios',
    dub: 'Sub • Doblado',
    category: 'temporada',
    poster: 'https://images.alphacoders.com/128/thumb-1920-1280387.png',
    url: 'https://www.crunchyroll.com/es/series/G4PH0WXVJ/spy-x-family',
    trailerId: 'ofXigq9aIpo'
  },
  {
    id: 'my_hero_academia',
    title: 'My Hero Academia (Boku no Hero)',
    genre: 'Superhéroes • Escuela • Shonen',
    episodes: '+140 Episodios',
    dub: 'Sub • Doblado',
    category: 'shonen',
    poster: 'https://images.alphacoders.com/134/thumb-1920-1345984.png',
    url: 'https://www.crunchyroll.com/es/series/G6NQ5DWZ6/my-hero-academia',
    trailerId: 'D5f78xRzD6E'
  },
  {
    id: 'bleach',
    title: 'Bleach: Thousand-Year Blood War',
    genre: 'Shinigamis • Espadas • Guerra',
    episodes: '366 + TYBW',
    dub: 'Sub • Doblado',
    category: 'shonen',
    poster: 'https://images2.alphacoders.com/127/thumb-1920-1279261.jpg',
    url: 'https://www.crunchyroll.com/es/series/G65V3D2Z6/bleach',
    trailerId: 'e8YBesRKq_U'
  },
  {
    id: 'death_note',
    title: 'Death Note',
    genre: 'Thriller Psicológico • Detectives • Suspenso',
    episodes: '37 Episodios',
    dub: 'Completo',
    category: 'popular',
    poster: 'https://images3.alphacoders.com/600/thumb-1920-600528.jpg',
    url: 'https://www.crunchyroll.com/es/series/G649XGG7R/death-note',
    trailerId: 'NlJZ-YgAt-c'
  }
];

let selectedAnime = null;
let currentCrCategory = 'all';

// Inicialización de botones de Crunchyroll TV
export function initCrunchyrollTV() {
  const btnBack = document.getElementById('btn-cr-back');
  if (btnBack) btnBack.addEventListener('click', closeCrunchyrollTV);

  const btnLogin = document.getElementById('btn-cr-login');
  if (btnLogin) {
    btnLogin.addEventListener('click', () => {
      if (window.showTvToast) window.showTvToast('Abriendo Login de Crunchyroll...', '🔑');
      window.location.href = 'https://www.crunchyroll.com/es/login';
    });
  }

  // Chips de filtro
  document.querySelectorAll('.cr-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.cr-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentCrCategory = chip.dataset.cat;
      renderAnimeCards();
    });
  });

  // Modal de acción
  const btnWatchCr = document.getElementById('cr-modal-btn-watch');
  const btnTrailer = document.getElementById('cr-modal-btn-trailer');
  const btnCloseModal = document.getElementById('cr-modal-btn-close');

  if (btnWatchCr) {
    btnWatchCr.addEventListener('click', () => {
      if (selectedAnime) {
        if (window.showTvToast) window.showTvToast(`Abriendo ${selectedAnime.title} en Crunchyroll...`, '🟠');
        window.location.href = selectedAnime.url;
      }
    });
  }

  if (btnTrailer) {
    btnTrailer.addEventListener('click', () => {
      if (selectedAnime) {
        closeAnimeModal();
        playVideo(selectedAnime.trailerId, `${selectedAnime.title} - Trailer Oficial`, 'Crunchyroll');
      }
    });
  }

  if (btnCloseModal) {
    btnCloseModal.addEventListener('click', closeAnimeModal);
  }
}

// Abrir vista Crunchyroll TV
export function openCrunchyrollTV() {
  document.getElementById('view-launcher').classList.add('hidden');
  document.getElementById('view-launcher').classList.remove('active');

  const crView = document.getElementById('view-crunchyroll-tv');
  crView.classList.remove('hidden');
  crView.classList.add('active');

  renderAnimeCards();
}

// Cerrar vista Crunchyroll TV y volver al Launcher
export function closeCrunchyrollTV() {
  closeAnimeModal();
  const crView = document.getElementById('view-crunchyroll-tv');
  crView.classList.add('hidden');
  crView.classList.remove('active');

  const launcher = document.getElementById('view-launcher');
  launcher.classList.remove('hidden');
  launcher.classList.add('active');

  if (window.refreshTvFocusables) window.refreshTvFocusables();
}

// Renderizar tarjetas de animes
function renderAnimeCards() {
  const container = document.getElementById('cr-anime-container');
  container.innerHTML = '';

  const filtered = currentCrCategory === 'all'
    ? ANIMES
    : ANIMES.filter(a => a.category === currentCrCategory);

  filtered.forEach((anime, idx) => {
    const card = document.createElement('div');
    card.className = 'cr-anime-card focusable';
    card.tabIndex = 0;
    card.dataset.index = idx;
    card.dataset.animeId = anime.id;

    card.innerHTML = `
      <div class="cr-poster-box">
        <img class="cr-poster" src="${anime.poster}" alt="${anime.title}" loading="lazy" />
        <span class="cr-badge-dub">${anime.dub}</span>
        <span class="cr-badge-ep">${anime.episodes}</span>
      </div>
      <div class="cr-card-info">
        <h4 class="cr-card-title">${anime.title}</h4>
        <p class="cr-card-genre">${anime.genre}</p>
      </div>
    `;

    card.addEventListener('click', () => openAnimeDetails(anime));
    container.appendChild(card);
  });

  if (window.refreshTvFocusables) window.refreshTvFocusables();
  const firstCard = container.querySelector('.cr-anime-card');
  if (firstCard) firstCard.focus();
}

// Abrir modal de detalles y reproducción
function openAnimeDetails(anime) {
  selectedAnime = anime;
  const modal = document.getElementById('cr-anime-modal');
  document.getElementById('cr-modal-title').textContent = anime.title;
  document.getElementById('cr-modal-desc').textContent = `${anime.genre} • ${anime.episodes} • ${anime.dub}`;
  modal.classList.remove('hidden');

  if (window.refreshTvFocusables) window.refreshTvFocusables();
  const btnWatch = document.getElementById('cr-modal-btn-watch');
  if (btnWatch) btnWatch.focus();
}

export function closeAnimeModal() {
  const modal = document.getElementById('cr-anime-modal');
  if (modal) modal.classList.add('hidden');
  selectedAnime = null;
  if (window.refreshTvFocusables) window.refreshTvFocusables();
}

export function isCrunchyrollViewActive() {
  const crView = document.getElementById('view-crunchyroll-tv');
  return crView && !crView.classList.contains('hidden');
}

export function isCrunchyrollModalActive() {
  const modal = document.getElementById('cr-anime-modal');
  return modal && !modal.classList.contains('hidden');
}
