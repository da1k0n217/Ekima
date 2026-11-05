import { browser } from '$app/environment';
import { writable } from 'svelte/store';
import { SAMPLE_STATIONS, type Station } from './stations';

const STORAGE_KEY = 'ekima.stations';

function readStations(): Station[] {
    if (!browser) return SAMPLE_STATIONS;

    try {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (!stored) return SAMPLE_STATIONS;
        const stations = JSON.parse(stored) as Station[];
        return Array.isArray(stations) ? stations : SAMPLE_STATIONS;
    } catch (error) {
        console.warn('保存済みの駅データを読み込めませんでした。', error);
        return SAMPLE_STATIONS;
    }
}

export const stations = writable<Station[]>(readStations());

if (browser) {
    stations.subscribe((value) => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
    });
}
