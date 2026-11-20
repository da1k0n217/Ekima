<script lang="ts">
    import { onDestroy, onMount } from 'svelte';
    import {
        findNearestStation,
        formatDistance,
        type Coordinates,
        type Station
    } from '$lib/stations';
    import { fetchStations } from '$lib/station-api';

    import { GlassButton } from 'glassy-liquid/svelte';

    let tracking = false;
    let location: Coordinates | null = null;
    let nearest: { station: Station; distance: number } | null = null;
    let errorMessage = '';
    let watchId: number | null = null;
    let nativeTrackingStarted = false;
    let candidateId: string | null = null;
    let candidateCount = 0;

    function updateNativeNotification(stationName: string | null) {
        if (typeof window === 'undefined') return;
        if (nativeTrackingStarted) return;
        const notifications = (
            window as Window & {
                EkimaNotifications?: {
                    requestNotificationPermission: () => void;
                    updateNearestStation: (name: string) => void;
                    clearNearestStation: () => void;
                    startBackgroundTracking: (stations: string) => void;
                    stopBackgroundTracking: () => void;
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

    function startNativeTracking(stationList: Station[]) {
        const notifications = (
            window as Window & {
                EkimaNotifications?: {
                    startBackgroundTracking: (stations: string) => void;
                };
            }
        ).EkimaNotifications;
        notifications?.startBackgroundTracking(JSON.stringify(stationList));
    }

    function stopNativeTracking() {
        const notifications = (
            window as Window & {
                EkimaNotifications?: {
                    stopBackgroundTracking: () => void;
                };
            }
        ).EkimaNotifications;
        notifications?.stopBackgroundTracking();
    }

    async function updateLocation(position: GeolocationPosition) {
        location = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy,
            timestamp: position.timestamp
        };
        errorMessage = '';

        let apiStations: Station[];
        try {
            apiStations = await fetchStations(location);
        } catch (error) {
            errorMessage = '駅情報を取得できませんでした。';
            console.error(error);
            return;
        }

        if (!nativeTrackingStarted) {
            startNativeTracking(apiStations);
            nativeTrackingStarted = true;
        }

        const result = findNearestStation(location, apiStations);
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
            stopNativeTracking();
            nativeTrackingStarted = false;
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
        stopNativeTracking();
        nativeTrackingStarted = false;
        updateNativeNotification(null);
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
        <section class="rounded-4xl border border-slate-700 bg-slate-900 p-5 shadow-lg">
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
            <GlassButton
                class="tracking-tight mt-5 w-full rounded-4xl px-4 py-3 font-bold transition-colors {tracking
                    ? 'bg-slate-700'
                    : 'bg-cyan-500 text-slate-950 '}"
                onclick={tracking ? stopTracking : startTracking}
            >
                {tracking ? '追跡を停止' : '位置情報の取得を開始'}
            </GlassButton>
            {#if errorMessage}
                <p class="mt-3 text-sm text-red-300" role="alert">{errorMessage}</p>
            {/if}
        </section>

    </div>
</main>
