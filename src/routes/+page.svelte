<script lang="ts">

</script>

<main class="app-shell">
    <header class="w-full py-4 bg-gray-900 flex items-center justify-center">
        <img src="/Logo.svg" alt="Logo" class="w-20 h-auto" />
    </header>

    <section class="content-area">
        <p class="content-text">Welcome to Ekima</p>
    </section>

    <footer class="bottom-nav" aria-label="Bottom navigation">
        <nav class="bottom-nav__content" aria-label="Main navigation">
            <a href="/" class="bottom-nav__item is-active" aria-current="page">
                <svg viewBox="0 0 24 24" aria-hidden="true" class="bottom-nav__icon">
                    <path d="M12 3l9 8h-3v10h-5v-6H11v6H6V11H3z"></path>
                </svg>
                <span>ホーム</span>
            </a>
            <a href="/settings" class="bottom-nav__item">
                <svg viewBox="0 0 24 24" aria-hidden="true" class="bottom-nav__icon">
                    <path d="M19.14 12.94a7.87 7.87 0 0 0 .05-.94 7.87 7.87 0 0 0-.05-.94l2.03-1.58a.5.5 0 0 0 .12-.64l-1.92-3.32a.5.5 0 0 0-.6-.22l-2.39.96a7.14 7.14 0 0 0-1.63-.94l-.36-2.54a.5.5 0 0 0-.49-.42h-3.84a.5.5 0 0 0-.49.42l-.36 2.54a7.14 7.14 0 0 0-1.63.94l-2.39-.96a.5.5 0 0 0-.6.22L2.71 8.84a.5.5 0 0 0 .12.64l2.03 1.58a7.87 7.87 0 0 0-.05.94 7.87 7.87 0 0 0 .05.94l-2.03 1.58a.5.5 0 0 0-.12.64l1.92 3.32a.5.5 0 0 0 .6.22l2.39-.96c.5.39 1.04.72 1.63.94l.36 2.54a.5.5 0 0 0 .49.42h3.84a.5.5 0 0 0 .49-.42l.36-2.54c.59-.22 1.13-.55 1.63-.94l2.39.96a.5.5 0 0 0 .6-.22l1.92-3.32a.5.5 0 0 0-.12-.64zM12 15.5A3.5 3.5 0 1 1 12 8.5a3.5 3.5 0 0 1 0 7z"></path>
                </svg>
                <span>設定</span>
            </a>
        </nav>
    </footer>
</main>

<style>
    .app-shell {
        --md-surface: #fef7ff;
        --md-surface-variant: #e8def8;
        --md-on-surface: #1d1b20;
        --md-on-surface-variant: #4a4458;

        min-height: 100vh;
        display: flex;
        flex-direction: column;
        background: linear-gradient(180deg, #1f2937 0%, #111827 100%);
    }

    .content-area {
        flex: 1;
        display: grid;
        place-items: center;
        padding: 2rem 1rem;
    }

    .content-text {
        color: #e5e7eb;
        letter-spacing: 0.02em;
    }

    .bottom-nav {
        margin-top: auto;
        background: var(--md-surface);
        color: var(--md-on-surface);
        border-top: 1px solid var(--md-surface-variant);
        box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.12), 0 -8px 16px rgba(0, 0, 0, 0.08);
    }

    .bottom-nav__content {
        width: min(640px, 100%);
        margin: 0 auto;
        min-height: 72px;
        padding: 0.5rem;
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        align-items: center;
        gap: 0.5rem;
    }

    .bottom-nav__item {
        color: var(--md-on-surface);
        text-decoration: none;
        font-size: 0.8rem;
        font-weight: 600;
        letter-spacing: 0.01em;
        border-radius: 16px;
        min-height: 56px;
        display: grid;
        place-items: center;
        gap: 0.2rem;
        transition: background-color 0.2s ease, color 0.2s ease;
    }

    .bottom-nav__item:hover {
        background: var(--md-surface-variant);
    }

    .bottom-nav__item.is-active {
        background: var(--md-surface-variant);
    }

    .bottom-nav__icon {
        width: 22px;
        height: 22px;
        fill: currentColor;
    }

    @media (max-width: 480px) {
        .bottom-nav__content {
            width: 100%;
            padding: 0.4rem;
        }
    }

</style>