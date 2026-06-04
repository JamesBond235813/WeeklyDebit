<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import CustListTableV2 from './cust-list-table-v2.vue';
// import CustListTable from './cust-list-table.vue';

defineOptions({ name: 'BizWorkCustomerPage' });
</script>
<template>
  <Page>
    <CustListTableV2 key="all-customers" />
  </Page>
  <!--  <Page>-->
  <!--    <CustListTable />-->
  <!--  </Page>-->
</template>
