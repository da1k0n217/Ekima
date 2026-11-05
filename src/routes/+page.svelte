<script lang="ts">
    import { onDestroy, onMount } from 'svelte';
    import { stations } from '$lib/station-store';
    import {
        findNearestStation,
        formatDistance,
        type Coordinates,
        type Station
    } from '$lib/stations';

    let tracking = false;
    let location: Coordinates | null = null;
    let nearest: { station: Station; distance: number } | null = null;
    let errorMessage = '';
    let watchId: number | null = null;
    let candidateId: string | null = null;
    let candidateCount = 0;
    let showForm = false;
    let newName = '';
    let newAddress = '';
    let newLatitude = '';
    let newLongitude = '';

    function updateNativeNotification(stationName: string | null) {
        if (typeof window === 'undefined') return;
        const notifications = (
            window as Window & {
                EkimaNotifications?: {
                    requestNotificationPermission: () => void;
                    updateNearestStation: (name: string) => void;
                    clearNearestStation: () => void;
                };
            }
        ).EkimaNotifications;
        if (!notifications) return;
        if (stationName) {
            notifications.updateNearestStation(stationName);
        } else {
            notifications.clearNearestStation();
        }
    }

    function requestNotificationPermission() {
        if (typeof window === 'undefined') return;
        const notifications = (
            window as Window & {
                EkimaNotifications?: {
                    requestNotificationPermission: () => void;
                };
            }
        ).EkimaNotifications;
        notifications?.requestNotificationPermission();
    }

    function updateLocation(position: GeolocationPosition) {
        location = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy,
            timestamp: position.timestamp
        };
        errorMessage = '';

        const result = findNearestStation(location, $stations);
        if (!result) {
            nearest = null;
            updateNativeNotification(null);
            return;
        }

        if (result.station.id === nearest?.station.id) {
            nearest = result;
            updateNativeNotification(result.station.name);
            candidateId = null;
            candidateCount = 0;
            return;
        }

        candidateCount =
            candidateId === result.station.id ? candidateCount + 1 : 1;
        candidateId = result.station.id;
        if (candidateCount >= 2 || nearest === null) {
            nearest = result;
            updateNativeNotification(result.station.name);
            candidateId = null;
            candidateCount = 0;
        }
    }

    function startTracking() {
        if (!navigator.geolocation) {
            errorMessage = 'この端末では位置情報を利用できません。';
            return;
        }

        errorMessage = '';
        requestNotificationPermission();
        watchId = navigator.geolocation.watchPosition(updateLocation, (error) => {
            errorMessage =
                error.code === error.PERMISSION_DENIED
                    ? '位置情報の権限が必要です。端末の設定から許可してください。'
                    : '位置情報を取得できませんでした。';
            tracking = false;
            watchId = null;
        }, {
            enableHighAccuracy: true,
            maximumAge: 5000,
            timeout: 15000
        });
        tracking = true;
    }

    function stopTracking() {
        if (watchId !== null) navigator.geolocation.clearWatch(watchId);
        watchId = null;
        tracking = false;
        candidateId = null;
        candidateCount = 0;
        updateNativeNotification(null);
    }

    function addStation() {
        const latitude = Number(newLatitude);
        const longitude = Number(newLongitude);
        if (
            !newName.trim() ||
            !Number.isFinite(latitude) ||
            !Number.isFinite(longitude) ||
            latitude < -90 ||
            latitude > 90 ||
            longitude < -180 ||
            longitude > 180
        ) {
            errorMessage = '駅名と正しい緯度・経度を入力してください。';
            return;
        }

        stations.update((items) => [
            ...items,
            {
                id: `station-${Date.now()}-${Math.random().toString(36).slice(2)}`,
                name: newName.trim(),
                address: newAddress.trim(),
                latitude,
                longitude
            }
        ]);
        newName = '';
        newAddress = '';
        newLatitude = '';
        newLongitude = '';
        showForm = false;
        errorMessage = '';
    }

    function removeStation(id: string) {
        stations.update((items) => items.filter((station) => station.id !== id));
        if (nearest?.station.id === id) nearest = null;
    }

    onMount(requestNotificationPermission);
    onDestroy(stopTracking);
</script>

<svelte:head>
    <title>Ekima - 最寄り駅</title>
</svelte:head>

<main class="min-h-screen bg-slate-950 text-slate-100">
    <header class="flex items-center justify-center border-b border-slate-800 bg-slate-900 px-4 py-4">
        <img src="/Logo.svg" alt="Ekima" class="h-auto w-20" />
    </header>

    <div class="mx-auto flex w-full max-w-2xl flex-col gap-5 px-4 py-6">
        <section class="rounded-2xl border border-slate-700 bg-slate-900 p-5 shadow-lg">
            <p class="text-sm text-slate-400">現在の最寄り駅</p>
            {#if nearest}
                <h1 class="mt-2 text-3xl font-bold">{nearest.station.name}</h1>
                <p class="mt-1 text-slate-400">{formatDistance(nearest.distance)}</p>
            {:else}
                <h1 class="mt-2 text-2xl font-semibold text-slate-300">
                    {tracking ? '位置情報を待っています' : '追跡を開始してください'}
                </h1>
            {/if}
            {#if location}
                <p class="mt-3 text-xs text-slate-500">
                    精度 ±{Math.round(location.accuracy)}m
                </p>
            {/if}
            <button
                class="mt-5 w-full rounded-xl px-4 py-3 font-semibold transition-colors {tracking
                    ? 'bg-slate-700 hover:bg-slate-600'
                    : 'bg-cyan-500 text-slate-950 hover:bg-cyan-400'}"
                onclick={tracking ? stopTracking : startTracking}
            >
                {tracking ? '追跡を停止' : '位置情報の取得を開始'}
            </button>
            {#if errorMessage}
                <p class="mt-3 text-sm text-red-300" role="alert">{errorMessage}</p>
            {/if}
        </section>

        <section class="rounded-2xl border border-slate-700 bg-slate-900 p-5">
            <div class="flex items-center justify-between">
                <h2 class="text-lg font-semibold">登録駅</h2>
                <button
                    class="rounded-lg border border-slate-600 px-3 py-2 text-sm hover:bg-slate-800"
                    onclick={() => (showForm = !showForm)}
                >
                    {showForm ? '閉じる' : '駅を追加'}
                </button>
            </div>

            {#if showForm}
                <form class="mt-4 grid gap-3" onsubmit={(event) => { event.preventDefault(); addStation(); }}>
                    <input bind:value={newName} placeholder="駅名" aria-label="駅名" required />
                    <input bind:value={newAddress} placeholder="住所（任意）" aria-label="住所" />
                    <div class="grid grid-cols-2 gap-3">
                        <input bind:value={newLatitude} type="number" step="any" placeholder="緯度" aria-label="緯度" required />
                        <input bind:value={newLongitude} type="number" step="any" placeholder="経度" aria-label="経度" required />
                    </div>
                    <button class="rounded-lg bg-cyan-500 px-4 py-2 font-semibold text-slate-950 hover:bg-cyan-400" type="submit">
                        保存
                    </button>
                </form>
            {/if}

            <ul class="mt-4 divide-y divide-slate-800">
                {#each $stations as station (station.id)}
                    <li class="flex items-center justify-between gap-3 py-3">
                        <div class="min-w-0">
                            <p class="font-medium">{station.name}</p>
                            <p class="truncate text-sm text-slate-400">{station.address || `${station.latitude}, ${station.longitude}`}</p>
                        </div>
                        <button
                            class="shrink-0 rounded-lg px-2 py-1 text-sm text-slate-400 hover:bg-red-950 hover:text-red-300"
                            aria-label={`${station.name}を削除`}
                            onclick={() => removeStation(station.id)}
                        >
                            削除
                        </button>
                    </li>
                {:else}
                    <li class="py-4 text-sm text-slate-400">登録駅がありません。</li>
                {/each}
            </ul>
        </section>
    </div>
</main>
