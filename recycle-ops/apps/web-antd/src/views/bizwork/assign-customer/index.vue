<script lang="ts" setup>
import { Page } from '@vben/common-ui';

// import CustListTable from '../customer/cust-list-table.vue';
import CustListTableV2 from '../customer/cust-list-table-v2.vue';
</script>
<template>
  <Page>
    <CustListTableV2 :favorite-flag="0" />
  </Page>
</template>
